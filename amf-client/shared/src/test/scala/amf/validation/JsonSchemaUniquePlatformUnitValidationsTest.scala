package amf.validation

import amf.client.parse.DefaultParserErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.remote.{Hint, OasYamlHint}
import amf.cycle.JsonSchemaSuite

import scala.concurrent.Future

class JsonSchemaUniquePlatformUnitValidationsTest extends UniquePlatformReportGenTest with JsonSchemaSuite{
  override val basePath: String    = "amf-client/shared/src/test/resources/validations/jsonschema/"
  override val reportsPath: String = "amf-client/shared/src/test/resources/validations/reports/jsonschema/"
  override val hint: Hint          = OasYamlHint // just here to respect interface

  test("minContains and maxContains should be bigger than 0") {
    validate("min-and-max-contains-non-negative.json", Some("min-and-max-contains-non-negative.report"))
  }

  test("minContains and maxContains should be numbers") {
    validate("min-and-max-contains-integers.json", Some("min-and-max-contains-integers.report"))
  }

  override protected def parse(path: String, eh: DefaultParserErrorHandler, finalHint: Hint): Future[BaseUnit] = {
    Future.successful(parseSchema(platform, path, "application/json", eh))
  }
}
