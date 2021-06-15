package amf.validation

import amf.core.client.common.validation.ProfileName
import amf.core.internal.remote.{Hint, Raml10YamlHint}

class LDSValidationTest extends UniquePlatformReportGenTest {

  override val basePath    = "file://amf-cli/shared/src/test/resources/validations/lds/"
  override val reportsPath = "amf-cli/shared/src/test/resources/validations/reports/lds/"
  override val hint: Hint  = Raml10YamlHint

  test("Missing key") {
    validate("api.raml", Some("api-lds1.report"), ProfileName("LDS"), Some("../profiles/lds.yaml"))
  }
}
