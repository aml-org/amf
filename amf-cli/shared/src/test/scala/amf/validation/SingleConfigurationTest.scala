package amf.validation

import amf.apicontract.client.scala.WebAPIConfiguration
import org.scalatest.{AsyncFunSuite, Matchers}

import scala.concurrent.ExecutionContext

class SingleConfigurationTest extends AsyncFunSuite with Matchers {
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("valid and invalid parsing errors with single instance") {
    val invalidApi = "file://amf-cli/shared/src/test/resources/parser-results/raml/error/map-key.raml"
    val validApi   = "file://amf-cli/shared/src/test/resources/validations/raml/valid-number-format.raml"
    val config     = WebAPIConfiguration.WebAPI()
    for {
      parsedInvalid <- config.baseUnitClient().parse(invalidApi)
      parsedValid   <- config.baseUnitClient().parse(validApi)
    } yield {
      assert(!parsedInvalid.conforms)
      assert(parsedValid.conforms)
    }
  }

}
