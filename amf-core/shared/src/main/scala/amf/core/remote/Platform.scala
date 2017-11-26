package amf.core.remote

import amf.core.lexer.CharSequenceStream
import amf.core.metamodel.Obj
import amf.core.model.document.BaseUnit
import amf.core.model.domain.{AmfObject, DomainElement}
import amf.core.vocabulary.Namespace
import org.mulesoft.common.io.{AsyncFile, FileSystem, SyncFile}

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}

/**
  *
  */
trait Platform {

  /** Underlying file system for platform. */
  val fs: FileSystem

  def exit(code: Int): Unit = System.exit(code)

  val wrappersRegistry: mutable.HashMap[String, (AmfObject) => AmfObjectWrapper] = mutable.HashMap.empty
  val wrappersRegistryFn: mutable.HashMap[(Obj) => Boolean, (AmfObject) => AmfObjectWrapper] = mutable.HashMap.empty

  def registerWrapper(model: Obj)(builder: (AmfObject) => AmfObjectWrapper) = wrappersRegistry.put(model.`type`.head.iri(), builder)
  def registerWrapperPredicate(p:(Obj) => Boolean)(builder: (AmfObject) => AmfObjectWrapper) = wrappersRegistryFn.put(p, builder)

  def wrap[T <: AmfObjectWrapper](entity: AmfObject): T =  entity match {
    case e: DomainElement => wrappersRegistry.get(e.meta.`type`.head.iri()) match {
      case Some(builder) => builder(entity).asInstanceOf[T]
      case None          => wrapFn(e)
    }
    case d: BaseUnit => wrappersRegistry.get(d.meta.`type`.head.iri()) match {
      case Some(builder) => builder(entity).asInstanceOf[T]
      case None          => wrapFn(d)
    }
    case _ => wrapFn(entity)
  }

  def wrapFn[T <: AmfObjectWrapper](entity: AmfObject): T = entity match {
    case e: DomainElement => wrappersRegistryFn.keys.find(p => p(e.meta)) match {
      case Some(k) => wrappersRegistryFn(k)(e).asInstanceOf[T]
      case None    => throw new Exception(s"Cannot find builder for object meta ${e.meta}")
    }
    case d: BaseUnit => wrappersRegistryFn.keys.find(p => p(d.meta)) match {
      case Some(k) => wrappersRegistryFn(k)(d).asInstanceOf[T]
      case None    => throw new Exception(s"Cannot find builder for object meta ${d.meta}")
    }
    case _ => throw new Exception(s"Cannot build object of type $entity")
  }

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

  def checkCache(url: String, eventualContent: () => Future[Content]): Future[Content] = {
    resourceCache.get(url) match {
      case Some(content) =>
        val p: Promise[Content] = Promise()
        p.success(content)
        p.future
      case None => eventualContent()
    }
  }

  // TODO: Removed from core @modularization
  // val dialectsRegistry: PlatformDialectRegistry

  //  @modularization
  // val validator: SHACLValidator

  /*  @modularization
  protected def setupValidationBase(validation: Validation): Future[Validation] =
    validation.loadValidationDialect().map(_ => validation)
  */

  def ensureFileAuthority(str: String): String = if (str.startsWith("file:")) { str } else { s"file://$str" }

  /** Test path resolution. */
  def resolvePath(path: String): String

  /** Register an alias for a namespace */
  def registerNamespace(alias: String, prefix: String): Option[Namespace] = Namespace.registerNamespace(alias, prefix)

  /** Resolve file on specified path. */
  protected def fetchFile(path: String): Future[Content]

  /** Resolve specified url. */
  protected def fetchHttp(url: String): Future[Content]

  /** Location where the helper functions for custom validations must be retrieved */
  protected def customValidationLibraryHelperLocation: String = "http://raml.org/amf/validation.js"

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

object Http {
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

object File {
  val FILE_PROTOCOL = "file://"

  def unapply(url: String): Option[String] = url match {
    case s if s.startsWith(FILE_PROTOCOL) =>
      val path = s.stripPrefix(FILE_PROTOCOL)
      Some(path)
    case _ => None
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

/**
  * Wrapper class for a platform with support to resolve the content of a URL without resolving it.
  * Used to implement the parseString functions, where the text for the provided URL is known
  */
case class StringContentPlatform(contentUrl: String, content: String, wrappedPlatform: Platform) extends Platform {

  /** Underlying file system for platform. */
  override val fs: FileSystem = wrappedPlatform.fs

  // TODO: Removed from platform @modularization
  // override val dialectsRegistry: PlatformDialectRegistry = wrappedPlatform.dialectsRegistry

  // @modularization
  //  override val validator: SHACLValidator = wrappedPlatform.validator

  override def resolvePath(path: String): String =
    if (path == contentUrl) {
      contentUrl
    } else {
      wrappedPlatform.resolvePath(path)
    }

  override protected def fetchFile(path: String): Future[Content] =
    if (path == contentUrl) {
      Future {
        Content(new CharSequenceStream(content), path)
      }
    } else {
      wrappedPlatform.resolve(File.FILE_PROTOCOL + path, None)
    }

  override protected def fetchHttp(url: String): Future[Content] =
    if (url == contentUrl) {
      Future {
        Content(new CharSequenceStream(content), url)
      }
    } else {
      wrappedPlatform.resolve(url, None)
    }

  override def tmpdir(): String = wrappedPlatform.tmpdir()

  override protected def writeFile(path: String, content: String): Future[Unit] =
    wrappedPlatform.write(File.FILE_PROTOCOL + path, content)
}

/** Unsupported file system. */
object UnsupportedFileSystem extends FileSystem {

  override def syncFile(path: String): SyncFile   = unsupported
  override def asyncFile(path: String): AsyncFile = unsupported
  override def separatorChar: Char                = unsupported

  private def unsupported = throw new Exception(s"Unsupported operation")
}
