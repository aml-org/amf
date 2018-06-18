package amf.core.remote

import amf.client.model.AmfObjectWrapper
import amf.client.remote.Content
import amf.core.metamodel.Obj
import amf.core.model.document.BaseUnit
import amf.core.model.domain.{AmfObject, DomainElement}
import amf.core.rdf.{RdfFramework, RdfModel}
import amf.core.validation.core.SHACLValidator
import amf.core.vocabulary.Namespace
import amf.internal.environment.Environment
import amf.internal.resource.ResourceLoader
import org.mulesoft.common.io.{AsyncFile, FileSystem, SyncFile}

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UnsupportedUrlScheme(url: String) extends Exception

trait FileMediaType {
  def mimeFromExtension(extension: String): Option[String] =
    extension match {
      case "json"                 => Some(Mimes.`APPLICATION/JSON`)
      case "yaml" | "yam" | "yml" => Some(Mimes.`APPLICATION/YAML`)
      case "raml"                 => Some(Mimes.`APPLICATION/RAML+YAML`)
      case "openapi"              => Some(Mimes.`APPLICATION/OPENAPI+JSON`)
      case "jsonld" | "amf"       => Some(Mimes.`APPLICATION/LD+JSONLD`)
      case _                      => None
    }

  def extension(path: String): Option[String] = {
    Some(path.lastIndexOf(".")).filter(_ > 0).map(dot => path.substring(dot + 1))
  }
}

object FileMediaType extends FileMediaType

/**
  *
  */
trait Platform extends FileMediaType {

  def findCharInCharSequence(s: CharSequence)(p: Char => Boolean): Option[Char]

  /** Underlying file system for platform. */
  val fs: FileSystem
  var testingCommandLine: Boolean = false

  def exit(code: Int): Unit = System.exit(code)

  def stdout(text: String): Unit = System.out.println(text)

  def stdout(e: Throwable): Unit = System.out.println(e)

  def stderr(text: String): Unit = System.err.println(text)

  def stderr(ex: Exception): Unit = System.err.println(ex)

  val wrappersRegistry: mutable.HashMap[String, (AmfObject) => AmfObjectWrapper]             = mutable.HashMap.empty
  val wrappersRegistryFn: mutable.HashMap[(Obj) => Boolean, (AmfObject) => AmfObjectWrapper] = mutable.HashMap.empty

  def registerWrapper(model: Obj)(builder: (AmfObject) => AmfObjectWrapper): Option[AmfObject => AmfObjectWrapper] =
    wrappersRegistry.put(model.`type`.head.iri(), builder)
  def registerWrapperPredicate(p: (Obj) => Boolean)(
      builder: (AmfObject) => AmfObjectWrapper): Option[AmfObject => AmfObjectWrapper] =
    wrappersRegistryFn.put(p, builder)

  def wrap[T <: AmfObjectWrapper](entity: AmfObject): T = entity match {
    case e: DomainElement =>
      wrappersRegistry.get(e.meta.`type`.head.iri()) match {
        case Some(builder) => builder(entity).asInstanceOf[T]
        case None          => wrapFn(e)
      }
    case d: BaseUnit =>
      wrappersRegistry.get(d.meta.`type`.head.iri()) match {
        case Some(builder) => builder(entity).asInstanceOf[T]
        case None          => wrapFn(d)
      }
    case null => null.asInstanceOf[T] // TODO solve this in a better way
    case _    => wrapFn(entity)
  }

  def wrapFn[T <: AmfObjectWrapper](entity: AmfObject): T = entity match {
    case e: DomainElement =>
      wrappersRegistryFn.keys.find(p => p(e.meta)) match {
        case Some(k) => wrappersRegistryFn(k)(e).asInstanceOf[T]
        case None => {
          throw new Exception(s"Cannot find builder for object meta ${e.meta}")
        }
      }
    case d: BaseUnit =>
      wrappersRegistryFn.keys.find(p => p(d.meta)) match {
        case Some(k) => wrappersRegistryFn(k)(d).asInstanceOf[T]
        case None    => throw new Exception(s"Cannot find builder for object meta ${d.meta}")
      }
    case _ => throw new Exception(s"Cannot build object of type $entity")
  }

  private def loaderConcat(url: String, loaders: Seq[ResourceLoader]): Future[Content] = loaders.toList match {
    case Nil         => Future.failed(new UnsupportedUrlScheme(url))
    case head :: Nil => head.fetch(url)
    case head :: tail =>
      head.fetch(url).recoverWith {
        case _ => loaderConcat(url, tail)
      }
  }

  /** Resolve remote url. */
  def resolve(url: String, env: Environment = Environment()): Future[Content] =
    loaderConcat(url, env.loaders.filter(_.accepts(url)))

  /** Platform out of the box [ResourceLoader]s */
  def loaders(): Seq[ResourceLoader]

  def ensureFileAuthority(str: String): String = if (str.startsWith("file:")) { str } else { s"file://$str" }

  /** Test path resolution. */
  def resolvePath(path: String): String

  /** encodes a complete uri. Not encodes chars like / */
  def encodeURI(url: String): String

  /** decode a complete uri. */
  def decodeURI(url: String): String

  /** encodes a uri component, including chars like / and : */
  def encodeURIComponent(url: String): String

  /** decodes a uri component */
  def decodeURIComponent(url: String): String

  /** validates and normalize complete url*/
  def normalizeURL(url: String): String

  /** normalize path method for file fetching in amf compiler*/
  def normalizePath(url: String): String

  /** Register an alias for a namespace */
  def registerNamespace(alias: String, prefix: String): Option[Namespace] = Namespace.registerNamespace(alias, prefix)

  // Optional RdfFramework
  var rdfFramework: Option[RdfFramework] = None

  /** Location where the helper functions for custom validations must be retrieved */
  protected def customValidationLibraryHelperLocation: String = "http://a.ml/amf/validation.js"

  /** Write specified content on given url. */
  def write(url: String, content: String): Future[Unit] = {
    url match {
      case File(path) => writeFile(path, content)
      case _          => Future.failed(new Exception(s"Unsupported write operation: $url"))
    }
  }

  /** Return temporary directory. */
  def tmpdir(): String

  /** Write specified content on specified file path. */
  protected def writeFile(path: String, content: String): Future[Unit] = fs.asyncFile(path).write(content)
}

object Platform {
  def base(url: String): Option[String] = Some(url.substring(0, url.lastIndexOf('/')))
}

object HttpParts {
  def unapply(uri: String): Option[(String, String, String)] = uri match {
    case url if url.startsWith("http://") || url.startsWith("https://") =>
      val protocol        = url.substring(0, url.indexOf("://") + 3)
      val rightOfProtocol = url.stripPrefix(protocol)
      val host =
        if (rightOfProtocol.contains("/")) rightOfProtocol.substring(0, rightOfProtocol.indexOf("/"))
        else rightOfProtocol
      val path = rightOfProtocol.replace(host, "")
      Some(protocol, host, path)
    case _ => None
  }
}

object File {
  val FILE_PROTOCOL = "file://"

  def unapply(url: String): Option[String] = {
    url match {
      case s if s.startsWith(FILE_PROTOCOL) =>
        val path = s.stripPrefix(FILE_PROTOCOL)
        Some(path)
      case _ => None
    }
  }
}

object Relative {
  def unapply(url: String): Option[String] = {
    url match {
      case s if !s.contains(":") => Some(s)
      case _                     => None
    }
  }
}

/** Unsupported file system. */
object UnsupportedFileSystem extends FileSystem {

  override def syncFile(path: String): SyncFile   = unsupported
  override def asyncFile(path: String): AsyncFile = unsupported
  override def separatorChar: Char                = unsupported

  private def unsupported = throw new Exception(s"Unsupported operation")
}

case class FileNotFound(cause: Exception) extends Exception(cause)

case class SocketTimeout(cause: Exception) extends Exception(cause)

case class NetworkError(cause: Throwable) extends Exception(cause)
