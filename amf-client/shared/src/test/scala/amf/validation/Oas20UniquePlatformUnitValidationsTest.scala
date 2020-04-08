package amf.validation

import amf.core.remote.{Hint, OasYamlHint}
import amf.{Oas20Profile, Oas30Profile}
import org.scalatest.Matchers

class Oas20UniquePlatformUnitValidationsTest extends UniquePlatformReportGenTest with Matchers {
  override val basePath: String    = "file://amf-client/shared/src/test/resources/validations/oas2/"
  override val reportsPath: String = "amf-client/shared/src/test/resources/validations/reports/oas2/"
  override val hint: Hint          = OasYamlHint

  test("Invalid security scheme type in Oas 20") {
    validate("invalid-security-scheme-type.json", Some("invalid-security-scheme-type.report"), Oas20Profile)
  }

  test("invalid ref inside paths object") {
    validate("invalid-ref-inside-paths-object.json", Some("invalid-ref-inside-paths-object.report"), Oas20Profile)
  }

  test("Required license name") {
    validate("mandatory-license-name.json", Some("mandatory-license-name.report"), Oas20Profile)
  }

  test("Empty array for parametrized security schemes") {
    validate("invalid-security-array.json", Some("invalid-security-array.report"), Oas20Profile)
  }

  test("Duplicated body parameter at endpoint level") {
    validate("invalid-duplicated-body-parameter.json", Some("invalid-duplicated-body-parameter.report"), Oas20Profile)
  }

  test("Recursive responses") {
    validate("recursive-responses/api.json", Some("recursive-responses.report"), Oas20Profile)
  }

  test("Endpoint outside paths node") {
    validate("endpoint-outside-paths.json", Some("endpoint-outside-paths.report"), Oas20Profile)
  }
}
