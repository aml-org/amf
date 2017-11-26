package amf.core.unsafe


import amf.core.lexer.CharSequenceStream
import amf.core.remote.{Content, Context, Platform, UnsupportedFileSystem}
import amf.core.validation.core.SHACLValidator
import org.mulesoft.common.io.FileSystem

import scala.concurrent.ExecutionContext.Implicits.global
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

  /** Resolve file on specified path. */
  override protected def fetchFile(path: String): Future[Content] = {
    Future {
      Content(new CharSequenceStream(content), path)
    }
  }

  /** Resolve specified url. */
  override protected def fetchHttp(url: String): Future[Content] = {
    fetchFile(url)
  }

  override def tmpdir(): String = throw new Exception("Unsupported tmpdir operation")

  override def resolve(url: String, context: Option[Context]): Future[Content] = {
    fetchFile(url)
  }

  // TODO: removed from core @modularization
  /*
  override val dialectsRegistry = wrappedPlatform match {
    case Some(p) => p.dialectsRegistry
    case None    => new TrunkDialectsRegistry(this)
  }

  override val validator = wrappedPlatform match {
    case Some(p) => p.validator
    case None    => new TrunkValidator()
  }
  */
}
