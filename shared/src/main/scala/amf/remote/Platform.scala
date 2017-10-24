package amf.remote

import amf.dialects.PlatformDialectRegistry
import amf.lexer.CharSequenceStream
import amf.validation.Validation
import amf.validation.core.SHACLValidator
import amf.vocabulary.Namespace

import scala.collection.mutable
import scala.concurrent.{Future, Promise}
import scala.concurrent.ExecutionContext.Implicits.global

/**
  *
  */
trait Platform {

  def exit(code: Int) = System.exit(code)

  /** Resolve remote url. */
  def resolve(url: String, context: Option[Context]): Future[Content] = {
    url match {
      case Http(_, _, _)                       => checkCache(url, () => fetchHttp(url))
      case File(path)                          => checkCache(path, () => fetchFile(path))
      case Relative(path) if context.isDefined => resolve(context.get resolve path, None)
      case _                                   => Future.failed(new Exception(s"Unsupported url: $url"))
    }
  }

  // Dealing with parsing of strings and HTTP caching
  val resourceCache: mutable.Map[String, Content] = mutable.HashMap()
  def cacheResourceText(url: String, text: String, mimeType: Option[String] = None): Unit = {
    val content = Content(new CharSequenceStream(url, text), url, mimeType)
    resourceCache += (url -> content)
  }
  def removeCacheResourceText(url: String): Option[Content] = resourceCache.remove(url)
  def resetResourceCache(): Unit                            = resourceCache.clear()

  def checkCache(url: String, eventualContent: () => Future[Content]): Future[_root_.amf.remote.Content] = {
    resourceCache.get(url) match {
      case Some(content) =>
        val p: Promise[Content] = Promise()
        p.success(content)
        p.future
      case None => eventualContent()
    }
  }

  val dialectsRegistry: PlatformDialectRegistry

  val validator: SHACLValidator

  protected def setupValidationBase(): Future[Validation] = {
    val validation = Validation(this)
    validation.loadValidationDialect().map { x => validation }
  }

  def ensureFileAuthority(str: String): String = if (str.startsWith("file:")) { str } else { s"file:/$str" }

  /** Test path resolution. */
  def resolvePath(path: String): String

  /** Register an alias for a namespace */
  def registerNamespace(alias: String, prefix: String) = Namespace.registerNamespace(alias, prefix)

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
