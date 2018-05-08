package amf.core.unsafe

import amf.client.remote.Content
import amf.core.remote.{Platform, UnsupportedFileSystem}
import amf.core.validation.core.SHACLValidator
import amf.internal.environment.Environment
import amf.internal.resource.ResourceLoader
import org.mulesoft.common.io.FileSystem

import scala.concurrent.Future

trait PlatformSecrets {
  val platform: Platform = PlatformBuilder()
}

// TODO: Removed from core @modularization
/*
class TrunkDialectsRegistry(platform: Platform) extends PlatformDialectRegistry(platform) {
  add(VocabularyLanguageDefinition)
  add(DialectLanguageDefinition)

  override def registerDialect(uri: String) = throw new Exception("Not supported in trunk platform")

  override def registerDialect(uri: String, dialect: String) = throw new Exception("Not supported in trunk platform")
}
 */

class TrunkValidator extends SHACLValidator {
  override def validate(data: String, dataMediaType: String, shapes: String, shapesMediaType: String) =
    throw new Exception("Error, validation is not supported")

  override def report(data: String, dataMediaType: String, shapes: String, shapesMediaType: String) =
    throw new Exception("Error, validation is not supported")

  /**
    * Registers a library in the validator
    *
    * @param url
    * @param code
    * @return
    */
  override def registerLibrary(url: String, code: String): Unit =
    throw new Exception("Error, validation is not supported")
}

case class TrunkPlatform(content: String, wrappedPlatform: Option[Platform] = None) extends Platform {

  /** Underlying file system for platform. */
  override val fs: FileSystem = UnsupportedFileSystem

  /** Test path resolution. */
  override def resolvePath(path: String): String = path

  override def tmpdir(): String = throw new Exception("Unsupported tmpdir operation")

  override def resolve(url: String, env: Environment = Environment()): Future[Content] =
    Future.successful(new Content(content, url))

  /** Platform out of the box [ResourceLoader]s */
  override def loaders(): Seq[ResourceLoader] = wrappedPlatform.map(_.loaders()).getOrElse(Seq())

  override def findCharInCharSequence(s: CharSequence)(p: Char => Boolean): Option[Char] =
    wrappedPlatform.flatMap(_.findCharInCharSequence(s)(p))

  /** encodes a complete uri. Not encodes chars like / */
  override def encodeURI(url: String): String = url

  /** decode a complete uri. */
  override def decodeURI(url: String): String = url

  /** encodes a uri component, including chars like / and : */
  override def encodeURIComponent(url: String): String = url

  /** decodes a uri component */
  override def decodeURIComponent(url: String): String = url

  override def normalizeURL(url: String): String = url

  override def normalizePath(url: String): String = url
}
