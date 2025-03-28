package amf.validation

import org.scalatest.matchers.should.Matchers

class Oas31UniquePlatformUnitValidationsTest extends UniquePlatformReportGenTest with Matchers {
  override val basePath: String    = "file://amf-cli/shared/src/test/resources/validations/oas31/"
  override val reportsPath: String = "amf-cli/shared/src/test/resources/validations/reports/oas31/"

  test("License identifier-url mutually exclusive fields validation") {
    validate("oas-31-license-identifier-url-validation.json", Some("oas-31-license-identifier-url-validation.report"))
  }
}
