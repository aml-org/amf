package amf.remote

import amf.dialects.PlatformDialectRegistry
import amf.validation.core.SHACLValidator

import scala.concurrent.Future

/**
  *
  */
trait Platform {

  /** Resolve remote url. */
  def resolve(url: String, context: Option[Context]): Future[Content] = {
    url match {
      case Http(_, _, _)                       => fetchHttp(url)
      case File(path)                          => fetchFile(path)
      case Relative(path) if context.isDefined => resolve(context.get resolve path, None)
      case _                                   => Future.failed(new Exception(s"Unsupported url: $url"))
    }
  }

  val dialectsRegistry: PlatformDialectRegistry

  val validator: SHACLValidator

  def ensureFileAuthority(str: String): String = if (str.startsWith("file:")) { str } else { s"file:/$str"}

  /** Test path resolution. */
  def resolvePath(path: String): String

  /** Resolve file on specified path. */
  protected def fetchFile(path: String): Future[Content]

  /** Resolve specified url. */
  protected def fetchHttp(url: String): Future[Content]

  /** Location where the helper functions for custom validations must be retrieved */
  protected def customValidationLibraryHelperLocation: String = "http://raml.org/amf/validation.js"

  /** Write specified content on given url. */
  def write(url: String, content: String): Future[String] = {
    url match {
      case File(path) => writeFile(path, content)
      case _          => Future.failed(new Exception(s"Unsupported write operation: $url"))
    }
  }

  /** Return temporary directory. */
  def tmpdir(): String

  /** Write specified content on specified file path. */
  protected def writeFile(path: String, content: String): Future[String]

  protected def mimeFromExtension(extension: String): Option[String] =
    extension match {
      case "json"         => Option(Mimes.`APPLICATION/JSON`)
      case "yaml" | "yam" => Option(Mimes.`APPLICATION/YAML`)
      case "raml"         => Option(Mimes.`APPLICATION/RAML+YAML`)
      case "openapi"      => Option(Mimes.`APPLICATION/OPENAPI+JSON`)
      case _              => None
    }

  protected def extension(path: String): Option[String] = {
    Some(path.lastIndexOf(".")).filter(_ > 0).map(dot => path.substring(dot + 1))
  }
}

object Platform {
  def base(url: String): Option[String] = Some(url.substring(0, url.lastIndexOf('/')))
}

protected object Http {
  def unapply(uri: String): Option[(String, String, String)] = uri match {
    case url if url.startsWith("http://") || url.startsWith("https://") =>
      val protocol        = url.substring(0, url.indexOf("://") + 3)
      val rightOfProtocol = url.stripPrefix(protocol)
      val host            = rightOfProtocol.substring(0, rightOfProtocol.indexOf("/"))
      val path            = rightOfProtocol.replace(host, "")
      Some(protocol, host, path)
    case _ => None
  }
}

protected object File {
  val FILE_PROTOCOL = "file://"

  def unapply(url: String): Option[String] = url match {
    case s if s.startsWith(FILE_PROTOCOL) =>
      val path = s.stripPrefix(FILE_PROTOCOL)
      Some(path)
    case _ => None
  }
}

private object Relative {
  def unapply(url: String): Option[String] = {
    url match {
      case s if !s.contains(":") => Some(s)
      case _                     => None
    }
  }
}
