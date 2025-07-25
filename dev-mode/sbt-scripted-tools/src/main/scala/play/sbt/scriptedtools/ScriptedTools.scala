/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */

package play.sbt.scriptedtools

import java.nio.file.Files
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer
import scala.sys.process.Process

import sbt._
import sbt.Keys._

import com.typesafe.sbt.packager.universal.UniversalPlugin.autoImport._
import play.sbt.routes.RoutesCompiler.autoImport._
import play.sbt.run.PlayRun

object ScriptedTools extends AutoPlugin {
  override def trigger = allRequirements

  override def projectSettings: Seq[Def.Setting[?]] = Def.settings(
    // This is copy/pasted from PekkoSnapshotRepositories since scripted tests also need
    // the snapshot resolvers in `cron` builds.
    // If this is a scheduled GitHub Action
    // https://docs.github.com/en/actions/learn-github-actions/environment-variables
    resolvers ++= sys.env
      .get("GITHUB_EVENT_NAME")
      .filter(_.equalsIgnoreCase("schedule"))
      .map(_ => Resolver.ApacheMavenSnapshotsRepo) // contains pekko(-http) snapshots
      .toSeq
  )

  def scalaVersionFromJavaProperties() =
    sys
      .props("scala.crossversions")
      .split(" ")
      .toSeq
      .filter(v => SemanticSelector(sys.props("scala.version")).matches(VersionNumber(v))) match {
      case Nil =>
        sys.error("Unable to detect scalaVersion! Did you pass scala.crossversions and scala.version Java properties?")
      case Seq(version) => version
      case multiple     =>
        sys.error(
          s"Multiple crossScalaVersions matched query '${sys.props("scala.version")}': ${multiple.mkString(", ")}"
        )
    }

  def callIndex(): Unit                   = callUrl("/")
  def applyEvolutions(path: String): Unit = callUrl(path)

  private val trustAllManager: TrustManager = new X509TrustManager() {
    def getAcceptedIssuers: Array[X509Certificate]                                = null
    def checkClientTrusted(certs: Array[X509Certificate], authType: String): Unit = ()
    def checkServerTrusted(certs: Array[X509Certificate], authType: String): Unit = ()
  }

  def setupSsl() = {
    val sc = SSLContext.getInstance("SSL")
    sc.init(null, Array(trustAllManager), null)
    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory)
  }

  def verifyResourceContains(
      path: String,
      status: Int,
      assertions: Seq[String],
      headers: (String, String)*
  ): Unit = {
    verifyResourceContainsImpl(false, path, status, assertions, headers, 0)
  }

  def verifyResourceContainsSsl(path: String, status: Int): Unit = {
    verifyResourceContainsImpl(true, path, status, Nil, Nil, 0)
  }

  @tailrec def verifyResourceContainsImpl(
      ssl: Boolean,
      path: String,
      status: Int,
      assertions: Seq[String],
      headers: Seq[(String, String)],
      attempts: Int
  ): Unit = {
    println(s"Attempt $attempts at $path")
    val messages = ListBuffer.empty[String]
    try {
      if (ssl) setupSsl()
      val loc = if (ssl) url(s"https://localhost:9443$path") else url(s"http://localhost:9000$path")

      val (requestStatus, contents) = callUrlImpl(loc, headers*)

      if (status == requestStatus) messages += s"Resource at $path returned $status as expected"
      else throw new RuntimeException(s"Resource at $path returned $requestStatus instead of $status")

      assertions.foreach { assertion =>
        if (contents.contains(assertion)) messages += s"Resource at $path contained $assertion"
        else throw new RuntimeException(s"Resource at $path didn't contain '$assertion':\n$contents")
      }

      messages.foreach(println)
    } catch {
      case e: Exception =>
        println(s"Got exception: $e. Cause was ${e.getCause}")
        // Using 30 max attempts so that we can give more chances to
        // the file watcher service. This is relevant when using the
        // default JDK watch service which does uses polling.
        if (attempts < 30) {
          TimeUnit.MILLISECONDS.sleep(500L)
          verifyResourceContainsImpl(ssl, path, status, assertions, headers, attempts + 1)
        } else {
          messages.foreach(println)
          println(s"After $attempts attempts:")
          throw e
        }
    }
  }

  def callUrl(path: String, headers: (String, String)*): (Int, String) = {
    callUrlImpl(url(s"http://localhost:9000$path"), headers*)
  }

  private def callUrlImpl(url: URL, headers: (String, String)*): (Int, String) = {
    val conn = url.openConnection().asInstanceOf[java.net.HttpURLConnection]
    conn.setConnectTimeout(10000)
    conn.setReadTimeout(10000)
    headers.foreach { case (k, v) => conn.setRequestProperty(k, v) }
    try {
      val status   = conn.getResponseCode
      val in       = if (conn.getResponseCode < 400) conn.getInputStream else conn.getErrorStream
      val contents =
        if (in == null) ""
        else {
          try IO.readStream(in)
          finally in.close()
        }
      (status, contents)
    } finally conn.disconnect()
  }

  val assertProcessIsStopped: Command = Command.args("assertProcessIsStopped", "") { (state, args) =>
    val pidFile = Project
      .extract(state)
      .get(Universal / com.typesafe.sbt.packager.universal.UniversalPlugin.autoImport.stagingDirectory) / "RUNNING_PID"
    if (!pidFile.exists())
      sys.error("RUNNING_PID file not found. Can't assert the process is stopped without knowing the process ID.")
    val pid = Files.readAllLines(pidFile.getAbsoluteFile.toPath).get(0)

    println("Preparing to stop Prod...")
    args match {
      case Seq("simulate-downing") => verifyResourceContains("/simulate-downing", 200, Nil)
      case _                       => PlayRun.stop(state)
    }
    println("Prod is stopping.")

    def processIsRunning(pid: String) = Process("jps").!!.split("\n").contains(s"$pid ProdServerStart")

    // Use a polling loop of at most 30sec. Without it,
    // the test moves on before the app has finished to shut down
    val secs = 10
    val end  = System.currentTimeMillis() + secs * 1000
    do {
      println(s"Is the PID file deleted already? ${!pidFile.exists()}")
      TimeUnit.SECONDS.sleep(3)
    } while (processIsRunning(pid) && System.currentTimeMillis() < end)

    if (processIsRunning(pid))
      throw new RuntimeException(s"Assertion failed: Process $pid didn't stop in $secs seconds.")

    state
  }

  val dumpRoutesSourceOnCompilationFailure = {
    val settings = Seq(
      compile := {
        compile.result.value match {
          case Value(v) => v
          case Inc(inc) =>
            // If there was a compilation error, dump generated routes files so we can read them
            ((Compile / routes / target).value ** AllPassFilter).filter(_.isFile).get.foreach { file =>
              println(s"Dumping $file:")
              IO.readLines(file).zipWithIndex.foreach {
                case (line, index) => println(f"${index + 1}%4d: $line")
              }
              println()
            }
            throw inc
        }
      }
    )
    Seq(Compile, Test).flatMap(inConfig(_)(settings))
  }

  def checkLines(source: String, target: String): Unit = {
    val sourceLines = IO.readLines(new File(source))
    val targetLines = IO.readLines(new File(target))

    println("Source:")
    println("-------")
    println(sourceLines.mkString("\n"))
    println("Target:")
    println("-------")
    println(targetLines.mkString("\n"))

    sourceLines.foreach { sl =>
      if (!targetLines.contains(sl)) {
        throw new RuntimeException(s"File $target didn't contain line:\n$sl")
      }
    }
  }

  def checkLinesPartially(source: String, target: String): Unit =
    checkLinesPartially(source, target, true)

  def checkLinesPartially(source: String, target: String, shouldContain: Boolean): Unit = {
    val sourceLines = IO.readLines(new File(source))
    val targetLines = IO.readLines(new File(target))

    println("Source:")
    println("-------")
    println(sourceLines.mkString("\n"))
    println("Target:")
    println("-------")
    println(targetLines.mkString("\n"))

    sourceLines.foreach { sl =>
      val contains = targetLines.exists(_.contains(sl))
      if ((contains && !shouldContain) || (!contains && shouldContain)) {
        throw new RuntimeException(s"File $target did${if (shouldContain) " not" else ""} partially contain line:\n$sl")
      }
    }
  }
}
