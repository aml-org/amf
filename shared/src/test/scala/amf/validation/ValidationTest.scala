package amf.validation

import amf.unsafe.PlatformSecrets
import org.scalatest.AsyncFunSuite

import scala.concurrent.ExecutionContext

class ValidationTest extends AsyncFunSuite with PlatformSecrets  {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath="file://shared/src/test/resources/vocabularies/"


  test("HERE_HERE Load dialect") {

    val validation = Validation(platform)
    try {
      for {
        _ <- validation.loadValidationDialect(basePath + "validation_dialect.raml")
        parsed <- validation.loadValidationProfile(basePath + "validation_profile_example.raml")
      } yield {
        println("LOADED!!!")
        assert(parsed != null)
      }
    } catch {
      case e:Exception => {
        e.printStackTrace()
        assert(e != null)
      }
    }
  }
}
