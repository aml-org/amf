package amf.unsafe

import amf.dialects.{DialectLanguageDefinition, DialectRegistry, PlatformDialectRegistry, VocabularyLanguageDefinition}
import amf.lexer.CharSequenceStream
import amf.remote._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait PlatformSecrets {
  val platform: Platform = PlatformBuilder()
}

class TrunkDialectsRegistry(platform: Platform) extends PlatformDialectRegistry(platform) {
  add(VocabularyLanguageDefinition)
  add(DialectLanguageDefinition)

  override def registerDialect(uri: String) = throw new Exception("Not supported in trunk platform")
}

case class TrunkPlatform(content: String) extends Platform {

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

  override protected def writeFile(path: String, content: String): Future[String] =
    throw new Exception("Unsupported write operation")

  override def tmpdir(): String = throw new Exception("Unsupported tmpdir operation")

  override def resolve(url: String, context: Option[Context]): Future[Content] = {
    fetchFile(url)
  }

  override val dialectsRegistry = new TrunkDialectsRegistry(this)
}
