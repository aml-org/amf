package amf.validation

import amf.client.parse.IgnoringErrorHandler
import amf.client.remod.{AMFGraphConfiguration, AMFResult}
import amf.client.environment.AMFConfiguration
import amf.client.remod.AMFResult
import amf.core.errorhandling.AMFErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.remote.{Hint, Oas20YamlHint}
import amf.cycle.JsonSchemaSuite

import scala.concurrent.Future

class JsonSchemaUniquePlatformUnitValidationsTest extends UniquePlatformReportGenTest with JsonSchemaSuite {
  override val basePath: String    = "amf-cli/shared/src/test/resources/validations/jsonschema/"
  override val reportsPath: String = "amf-cli/shared/src/test/resources/validations/reports/jsonschema/"
  override val hint: Hint          = Oas20YamlHint // just here to respect interface

  test("minContains and maxContains should be bigger than 0") {
    validate("min-and-max-contains-non-negative.json", Some("min-and-max-contains-non-negative.report"))
  }

  // this test has parsing error that are not taken into account in report.
  test("minContains and maxContains should be numbers") {
    validate("min-and-max-contains-integers.json", Some("min-and-max-contains-integers.report"))
  }

  test("unused facets in validation throw warning") {
    validate("unused-validation-facets.json", Some("unused-validation-facets.report"))
  }

  override protected def parse(path: String, conf: AMFConfiguration, finalHint: Hint): Future[AMFResult] = {
    // uses IgnoringErrorHandler as this was the previous (possibly unintentional) behaviour, but now made explicit
    Future.successful(
      parseSchema(platform, path, "application/json", conf.withErrorHandlerProvider(() => IgnoringErrorHandler)))
  }
}
