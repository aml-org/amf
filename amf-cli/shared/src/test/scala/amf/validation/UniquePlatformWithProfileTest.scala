package amf.validation

import amf.core.client.common.validation.ProfileName
import amf.core.client.scala.validation.AMFValidationReport
import amf.core.internal.remote.{Hint, Raml10YamlHint}
import amf.validation.internal.emitters.ValidationReportJSONLDEmitter

class UniquePlatformWithProfileTest extends UniquePlatformReportGenTest {

  override val basePath: String    = "file://amf-cli/shared/src/test/resources/validations/"
  override val reportsPath: String = "amf-cli/shared/src/test/resources/validations/reports/with-profiles/"

  private def validate(api: String, profile: String, profileFile: Option[String]) =
    super.validate(api, None, ProfileName(profile), profileFile)

  override protected def generate(report: AMFValidationReport): String = ValidationReportJSONLDEmitter.emitJSON(report)

  test("Validation test, ignore profile") {
    validate("data/error1.raml", "Test Profile", Some("data/error1_ignore_profile.raml"))
  }

  test("Raml Vocabulary") {
    validate("data/error1.raml", "Test Profile", Some("data/custom_function_validation_success.raml"))
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

  override val hint: Hint = Raml10YamlHint
}
