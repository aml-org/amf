package amf.validation

import org.scalatest.matchers.should.Matchers

class Oas31UniquePlatformUnitValidationsTest extends UniquePlatformReportGenTest with Matchers {
  override val basePath: String    = "file://amf-cli/shared/src/test/resources/validations/oas31/"
  override val reportsPath: String = "amf-cli/shared/src/test/resources/validations/reports/oas31/"

  test("oas 31 items field must be an object") {
    validate("items-property-array.yaml", Some("items-property-array.report"))
  }

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

  test("paths node is no longer required") {
    validate("no-paths.yaml")
  }

  test("paths, components, or webhooks required") {
    validate("nothing.yaml", Some("nothing.report"))
  }

  test("responses node is no longer required") {
    validate("no-responses.yaml")
  }

  test("invalid path param name in path") {
    validate("invalid-path-param-name-path.yaml", Some("invalid-path-param-name-path.report"))
  }

  test("invalid path param name in endpoint/operation") {
    validate("invalid-path-param-name-end-ope.yaml", Some("invalid-path-param-name-end-ope.report"))
  }

  test("invalid path param name in path and endpoint/operation") {
    validate("invalid-path-param-name-all.yaml", Some("invalid-path-param-name-all.report"))
  }
}
