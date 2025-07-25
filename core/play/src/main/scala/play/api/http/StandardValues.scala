/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */

package play.api.http

/**
 * Defines common HTTP Content-Type header values, according to the current available Codec.
 */
object ContentTypes extends ContentTypes

/** Defines common HTTP Content-Type header values, according to the current available Codec. */
trait ContentTypes {
  import play.api.mvc.Codec

  /**
   * Content-Type of text.
   */
  def TEXT(implicit codec: Codec) = withCharset(MimeTypes.TEXT)

  /**
   * Content-Type of html.
   */
  def HTML(implicit codec: Codec) = withCharset(MimeTypes.HTML)

  /**
   * Content-Type of xhtml.
   */
  def XHTML(implicit codec: Codec) = withCharset(MimeTypes.XHTML)

  /**
   * Content-Type of xml.
   */
  def XML(implicit codec: Codec) = withCharset(MimeTypes.XML)

  /**
   * Content-Type of css.
   */
  def CSS(implicit codec: Codec) = withCharset(MimeTypes.CSS)

  /**
   * Content-Type of javascript.
   */
  def JAVASCRIPT(implicit codec: Codec) = withCharset(MimeTypes.JAVASCRIPT)

  /**
   * Content-Type of server sent events.
   */
  def EVENT_STREAM(implicit codec: Codec) = withCharset(MimeTypes.EVENT_STREAM)

  /**
   * Content-Type of application cache.
   */
  val CACHE_MANIFEST = withCharset(MimeTypes.CACHE_MANIFEST)(using Codec.utf_8)

  /**
   * Content-Type of json. This content type does not define a charset parameter.
   */
  val JSON = MimeTypes.JSON

  /**
   * Content-Type of form-urlencoded. This content type does not define a charset parameter.
   */
  val FORM = MimeTypes.FORM

  /**
   * Content-Type of binary data.
   */
  val BINARY = MimeTypes.BINARY

  /**
   * @return the `codec` charset appended to `mimeType`
   */
  def withCharset(mimeType: String)(implicit codec: Codec) = s"$mimeType; charset=${codec.charset}"
}

/**
 * Standard HTTP Verbs
 */
object HttpVerbs extends HttpVerbs

/**
 * Standard HTTP Verbs
 */
trait HttpVerbs {
  val GET     = "GET"
  val POST    = "POST"
  val PUT     = "PUT"
  val PATCH   = "PATCH"
  val DELETE  = "DELETE"
  val HEAD    = "HEAD"
  val OPTIONS = "OPTIONS"
}

/** Common HTTP MIME types */
object MimeTypes extends MimeTypes

/** Common HTTP MIME types */
trait MimeTypes {

  /**
   * Content-Type of text.
   */
  val TEXT = "text/plain"

  /**
   * Content-Type of html.
   */
  val HTML = "text/html"

  /**
   * Content-Type of json.
   */
  val JSON = "application/json"

  /**
   * Content-Type of xml.
   */
  val XML = "application/xml"

  /**
   * Content-Type of xhtml.
   */
  val XHTML = "application/xhtml+xml"

  /**
   * Content-Type of css.
   */
  val CSS = "text/css"

  /**
   * Content-Type of javascript.
   */
  val JAVASCRIPT = "application/javascript"

  /**
   * Content-Type of form-urlencoded.
   */
  val FORM = "application/x-www-form-urlencoded"

  /**
   * Content-Type of server sent events.
   */
  val EVENT_STREAM = "text/event-stream"

  /**
   * Content-Type of binary data.
   */
  val BINARY = "application/octet-stream"

  /**
   * Content-Type of application cache.
   */
  val CACHE_MANIFEST = "text/cache-manifest"
}

/**
 * Defines all standard HTTP status codes, with additional helpers for determining the type of status.
 */
object Status extends Status {
  def isInformational(status: Int): Boolean = status / 100 == 1
  def isSuccessful(status: Int): Boolean    = status / 100 == 2
  def isRedirect(status: Int): Boolean      = status / 100 == 3
  def isClientError(status: Int): Boolean   = status / 100 == 4
  def isServerError(status: Int): Boolean   = status / 100 == 5
}

/**
 * Defines all standard HTTP status codes.
 *
 * See <a href="https://tools.ietf.org/html/rfc7231">RFC 7231</a> and <a href="https://tools.ietf.org/html/rfc6585">RFC 6585</a>.
 */
trait Status {
  val CONTINUE            = 100
  val SWITCHING_PROTOCOLS = 101

  val OK                            = 200
  val CREATED                       = 201
  val ACCEPTED                      = 202
  val NON_AUTHORITATIVE_INFORMATION = 203
  val NO_CONTENT                    = 204
  val RESET_CONTENT                 = 205
  val PARTIAL_CONTENT               = 206
  val MULTI_STATUS                  = 207

  val MULTIPLE_CHOICES   = 300
  val MOVED_PERMANENTLY  = 301
  val FOUND              = 302
  val SEE_OTHER          = 303
  val NOT_MODIFIED       = 304
  val USE_PROXY          = 305
  val TEMPORARY_REDIRECT = 307
  val PERMANENT_REDIRECT = 308

  val BAD_REQUEST                     = 400
  val UNAUTHORIZED                    = 401
  val PAYMENT_REQUIRED                = 402
  val FORBIDDEN                       = 403
  val NOT_FOUND                       = 404
  val METHOD_NOT_ALLOWED              = 405
  val NOT_ACCEPTABLE                  = 406
  val PROXY_AUTHENTICATION_REQUIRED   = 407
  val REQUEST_TIMEOUT                 = 408
  val CONFLICT                        = 409
  val GONE                            = 410
  val LENGTH_REQUIRED                 = 411
  val PRECONDITION_FAILED             = 412
  val REQUEST_ENTITY_TOO_LARGE        = 413
  val REQUEST_URI_TOO_LONG            = 414
  val UNSUPPORTED_MEDIA_TYPE          = 415
  val REQUESTED_RANGE_NOT_SATISFIABLE = 416
  val EXPECTATION_FAILED              = 417
  val IM_A_TEAPOT                     = 418
  val UNPROCESSABLE_ENTITY            = 422
  val LOCKED                          = 423
  val FAILED_DEPENDENCY               = 424
  val UPGRADE_REQUIRED                = 426
  val PRECONDITION_REQUIRED           = 428
  val TOO_MANY_REQUESTS               = 429
  val REQUEST_HEADER_FIELDS_TOO_LARGE = 431

  val INTERNAL_SERVER_ERROR           = 500
  val NOT_IMPLEMENTED                 = 501
  val BAD_GATEWAY                     = 502
  val SERVICE_UNAVAILABLE             = 503
  val GATEWAY_TIMEOUT                 = 504
  val HTTP_VERSION_NOT_SUPPORTED      = 505
  val INSUFFICIENT_STORAGE            = 507
  val NETWORK_AUTHENTICATION_REQUIRED = 511
}

/** Defines all standard HTTP headers. */
object HeaderNames extends HeaderNames

/** Defines all standard HTTP headers. */
trait HeaderNames {
  val ACCEPT          = "Accept"
  val ACCEPT_CHARSET  = "Accept-Charset"
  val ACCEPT_ENCODING = "Accept-Encoding"
  val ACCEPT_LANGUAGE = "Accept-Language"
  val ACCEPT_RANGES   = "Accept-Ranges"
  val AGE             = "Age"
  val ALLOW           = "Allow"
  val AUTHORIZATION   = "Authorization"

  val CACHE_CONTROL             = "Cache-Control"
  val CONNECTION                = "Connection"
  val CONTENT_DISPOSITION       = "Content-Disposition"
  val CONTENT_ENCODING          = "Content-Encoding"
  val CONTENT_LANGUAGE          = "Content-Language"
  val CONTENT_LENGTH            = "Content-Length"
  val CONTENT_LOCATION          = "Content-Location"
  val CONTENT_MD5               = "Content-MD5"
  val CONTENT_RANGE             = "Content-Range"
  val CONTENT_TRANSFER_ENCODING = "Content-Transfer-Encoding"
  val CONTENT_TYPE              = "Content-Type"
  val COOKIE                    = "Cookie"

  val DATE = "Date"

  val ETAG    = "ETag"
  val EXPECT  = "Expect"
  val EXPIRES = "Expires"

  val FROM = "From"

  val HOST = "Host"

  val IF_MATCH            = "If-Match"
  val IF_MODIFIED_SINCE   = "If-Modified-Since"
  val IF_NONE_MATCH       = "If-None-Match"
  val IF_RANGE            = "If-Range"
  val IF_UNMODIFIED_SINCE = "If-Unmodified-Since"

  val LAST_MODIFIED = "Last-Modified"
  val LINK          = "Link"
  val LOCATION      = "Location"

  val MAX_FORWARDS = "Max-Forwards"

  val PRAGMA              = "Pragma"
  val PROXY_AUTHENTICATE  = "Proxy-Authenticate"
  val PROXY_AUTHORIZATION = "Proxy-Authorization"

  val RANGE       = "Range"
  val REFERER     = "Referer"
  val RETRY_AFTER = "Retry-After"

  val SERVER = "Server"

  val SET_COOKIE  = "Set-Cookie"
  val SET_COOKIE2 = "Set-Cookie2"

  val TE                = "Te"
  val TRAILER           = "Trailer"
  val TRANSFER_ENCODING = "Transfer-Encoding"

  val UPGRADE    = "Upgrade"
  val USER_AGENT = "User-Agent"

  val VARY = "Vary"
  val VIA  = "Via"

  val WARNING          = "Warning"
  val WWW_AUTHENTICATE = "WWW-Authenticate"

  val FORWARDED         = "Forwarded"
  val X_FORWARDED_FOR   = "X-Forwarded-For"
  val X_FORWARDED_HOST  = "X-Forwarded-Host"
  val X_FORWARDED_PORT  = "X-Forwarded-Port"
  val X_FORWARDED_PROTO = "X-Forwarded-Proto"

  val X_REQUESTED_WITH = "X-Requested-With"

  val ACCESS_CONTROL_ALLOW_ORIGIN      = "Access-Control-Allow-Origin"
  val ACCESS_CONTROL_EXPOSE_HEADERS    = "Access-Control-Expose-Headers"
  val ACCESS_CONTROL_MAX_AGE           = "Access-Control-Max-Age"
  val ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials"
  val ACCESS_CONTROL_ALLOW_METHODS     = "Access-Control-Allow-Methods"
  val ACCESS_CONTROL_ALLOW_HEADERS     = "Access-Control-Allow-Headers"

  val ORIGIN                         = "Origin"
  val ACCESS_CONTROL_REQUEST_METHOD  = "Access-Control-Request-Method"
  val ACCESS_CONTROL_REQUEST_HEADERS = "Access-Control-Request-Headers"

  val STRICT_TRANSPORT_SECURITY = "Strict-Transport-Security"

  val X_FRAME_OPTIONS                   = "X-Frame-Options"
  val X_XSS_PROTECTION                  = "X-XSS-Protection"
  val X_CONTENT_TYPE_OPTIONS            = "X-Content-Type-Options"
  val X_PERMITTED_CROSS_DOMAIN_POLICIES = "X-Permitted-Cross-Domain-Policies"
  val REFERRER_POLICY                   = "Referrer-Policy"

  val CONTENT_SECURITY_POLICY                        = "Content-Security-Policy"
  val CONTENT_SECURITY_POLICY_REPORT_ONLY: String    = "Content-Security-Policy-Report-Only"
  val X_CONTENT_SECURITY_POLICY_NONCE_HEADER: String = "X-Content-Security-Policy-Nonce"
}

/**
 * Defines HTTP protocol constants
 */
object HttpProtocol extends HttpProtocol

/**
 * Defines HTTP protocol constants
 */
trait HttpProtocol {
  // Versions
  val HTTP_1_0 = "HTTP/1.0"
  val HTTP_1_1 = "HTTP/1.1"
  val HTTP_2_0 = "HTTP/2.0"

  // Other HTTP protocol values
  val CHUNKED = "chunked"
}
