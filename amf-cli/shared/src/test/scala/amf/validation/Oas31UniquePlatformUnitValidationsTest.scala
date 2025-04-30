package amf.validation

import org.scalatest.matchers.should.Matchers

class Oas31UniquePlatformUnitValidationsTest extends UniquePlatformReportGenTest with Matchers {
  override val basePath: String    = "file://amf-cli/shared/src/test/resources/validations/oas31/"
  override val reportsPath: String = "amf-cli/shared/src/test/resources/validations/reports/oas31/"

  test("License identifier-url mutually exclusive fields validation") {
    validate("oas-31-license-identifier-url-validation.json", Some("oas-31-license-identifier-url-validation.report"))
  }

  test("Server Variable Object must have a default field") {
    validate("server-variables-default.yaml", Some("server-variables-default.report"))
  }

  test("Server Variable Object enum field can't be an empty array") {
    validate("server-variables-empty-enum.yaml", Some("server-variables-empty-enum.report"))
  }

  test("nullable key should not be valid anymore") {
    validate("nullable-key.yaml", Some("nullable-key.report"))
  }
}
