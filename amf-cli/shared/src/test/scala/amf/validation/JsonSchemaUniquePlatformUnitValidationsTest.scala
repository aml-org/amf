package amf.validation

import amf.apicontract.client.scala.AMFConfiguration
import amf.core.client.scala.AMFParseResult
import amf.core.internal.remote.Mimes._
import amf.core.internal.remote.{Hint, Oas20YamlHint}
import amf.cycle.JsonSchemaSuite

import scala.concurrent.Future

class JsonSchemaUniquePlatformUnitValidationsTest extends UniquePlatformReportGenTest with JsonSchemaSuite {
  override val basePath: String    = "amf-cli/shared/src/test/resources/validations/jsonschema/"
  override val reportsPath: String = "amf-cli/shared/src/test/resources/validations/reports/jsonschema/"
  override val hint: Hint          = Oas20YamlHint // just here to respect interface

  test("minContains and maxContains should be bigger than 0") {
    validate("min-and-max-contains-non-negative.json", Some("min-and-max-contains-non-negative.report"))
  }

  test("minContains and maxContains should be numbers") {
    validate(
      "min-and-max-contains-integers.json",
      Some("min-and-max-contains-integers.report"),
      hideValidationResultsIfParseNotConforms = false
    )
  }

  test("unused facets in validation throw warning") {
    validate("unused-validation-facets.json", Some("unused-validation-facets.report"))
  }

  test("boolean schemas not supported in JSON Schema below version draft-6") {
    validate("boolean-schemas.json", Some("boolean-schemas.report"))
  }

  override protected def parse(path: String, conf: AMFConfiguration, finalHint: Hint): Future[AMFParseResult] = {
    // uses IgnoringErrorHandler as this was the previous (possibly unintentional) behaviour, but now made explicit
    Future.successful(parseSchema(platform, path, `application/json`, conf))
  }
}
