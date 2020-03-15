package amf.validation
import amf.ProfileName
import amf.core.remote.{Hint, RamlYamlHint}

class CustomValidationTest extends UniquePlatformReportGenTest {

  override val basePath    = "file://amf-client/shared/src/test/resources/validations/"
  override val reportsPath = "amf-client/shared/src/test/resources/validations/reports/"
  override val hint: Hint  = RamlYamlHint


  test("HERE_HERE Load dialect") {
    validate("mercury.raml", Some("mercury1.report"), ProfileName("mercury"), Some("profiles/mercury.yaml"))
  }

}
