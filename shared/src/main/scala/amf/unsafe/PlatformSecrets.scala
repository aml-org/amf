package amf.unsafe

import amf.lexer.CharSequenceStream
import amf.remote._
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

trait PlatformSecrets {
  val platform: Platform = PlatformBuilder()

//  object builders {
//
//    def webApi: BaseWebApiBuilder         = new WebApiBuilder
//    def license: LicenseBuilder           = LicenseBuilder()
//    def creativeWork: CreativeWorkBuilder = CreativeWorkBuilder()
//    def organization: OrganizationBuilder = OrganizationBuilder()
//    def endPoint: EndPointBuilder         = EndPointBuilder()
//  }
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

  /** Write specified content on specified file path. */
  override protected def writeFile(path: String, content: String): Future[String] = ???

  override def resolve(url: String, context: Option[Context]): Future[Content] = {
    fetchFile(url)
  }

}