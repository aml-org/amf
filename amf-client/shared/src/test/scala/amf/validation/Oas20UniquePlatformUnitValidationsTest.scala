package amf.validation

import amf.Oas20Profile
import amf.core.remote.{Hint, OasYamlHint}
import org.scalatest.Matchers

class Oas20UniquePlatformUnitValidationsTest extends UniquePlatformReportGenTest with Matchers {
  override val basePath: String    = "file://amf-client/shared/src/test/resources/validations/oas2/"
  override val reportsPath: String = "amf-client/shared/src/test/resources/validations/reports/oas2/"
  override val hint: Hint          = OasYamlHint

  test("Oas path uri is invalid") {
    validate("invalid-endpoint-path-still-parses.json",
             Some("invalid-endpoint-path-still-parses.report"),
             Oas20Profile)
  }

  test("invalid fields when security type is basic") {
    validate("security/invalid-fields-basic-type.json", Some("invalid-fields-basic-type.report"), Oas20Profile)
  }

  test("Invalid security scheme type") {
    validate("security/invalid-security-scheme-type.json", Some("invalid-security-scheme-type.report"), Oas20Profile)
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

  test("Reference a yaml file") {
    validate("yaml-ref/api.yaml", None, Oas20Profile)
  }

  test("Nested yaml refs") {
    validate("nested-yaml-refs/api.yaml", None, Oas20Profile)
  }

  test("SecuritySchema scope in implemetation that is not defined in declaration") {
    validate("security-schema-scopes.yaml", Some("security-schema-scopes.report"), Oas20Profile)
  }

  test("Invalid header names according to RFC-7230") {
    validate("invalid-header-names.yaml", Some("invalid-header-names.report"), Oas20Profile)
  }
}
