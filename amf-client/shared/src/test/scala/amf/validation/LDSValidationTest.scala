package amf.validation


import amf.ProfileName
import amf.core.remote.{Hint, RamlYamlHint}

class LDSValidationTest extends UniquePlatformReportGenTest {

  override val basePath = "file://amf-client/shared/src/test/resources/validations/lds/"
  override val reportsPath = "amf-client/shared/src/test/resources/validations/reports/lds/"
  override val hint: Hint = RamlYamlHint


  test("Missing key") {
    validate("api.raml", Some("api-lds1.report"), ProfileName("LDS"), Some("../profiles/lds.yaml"))
  }
}

