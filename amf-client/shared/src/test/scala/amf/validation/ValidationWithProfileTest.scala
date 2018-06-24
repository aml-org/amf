package amf.validation

import amf.ProfileNames.ProfileName
import amf.core.remote.{Hint, RamlYamlHint}
import amf.core.validation.AMFValidationReport
import amf.plugins.features.validation.emitters.ValidationReportJSONLDEmitter

class ValidationWithProfileTest extends ValidationReportGenTest {

  override val basePath: String    = "file://amf-client/shared/src/test/resources/validations/"
  override val reportsPath: String = "amf-client/shared/src/test/resources/validations/reports/with-profiles/"

  private def validate(api: String, profile: String, profileFile: Option[String]) =
    super.validate(api, None, ProfileName(profile), profileFile)

  override protected def generate(report: AMFValidationReport): String = ValidationReportJSONLDEmitter.emitJSON(report)

  test("Validation test, ignore profile") {
    validate("data/error1.raml", "Test Profile", Some("data/error1_ignore_profile.raml"))
  }

  test("Raml Vocabulary") {
    validate("data/error1.raml", "Test Profile", Some("data/custom_function_validation_success.raml"))
  }

  test("Custom function validation failure test") {
    validate("data/error1.raml",
             Some("custom-validation-error.report"),
             ProfileName("Test Profile"),
             Some("data/custom_function_validation_error.raml"))
  }

  test("Validation test, custom validation profile") {
    validate("data/error1.raml",
             Some("error1-custom-validation.report"),
             ProfileName("Test Profile"),
             Some("data/error1_custom_validation_profile.raml"))
  }

  test("Banking example validation") {
    validate("banking/api.raml", Some("baking-api.report"), ProfileName("Banking"), Some("banking/profile.raml"))
  }

  ignore("Example JS library validations") {
    validate("libraries/api.raml",
             Some("libraries-profile.report"),
             ProfileName("Test"),
             Some("libraries/profile.raml"))
//      assert(!report.conforms)
//      assert(report.results.length == 1)
  }

  test("JSON API Validation positive case") {
    validate("jsonapi/correct.raml", None, ProfileName("JSON API"), Some("jsonapi/jsonapi_profile.raml"))
  }

  test("JSON API Validation negative case") {
    validate("jsonapi/incorrect.raml",
             Some("jsonapi-incorrect.report"),
             ProfileName("JSON API 1.0"),
             Some("jsonapi/jsonapi_profile.raml"))
  }
  override val hint: Hint = RamlYamlHint
}
