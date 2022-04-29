package amf.validation

import amf.core.client.common.validation.Oas20Profile
import amf.core.internal.remote.{Hint, Oas20JsonHint, Oas20YamlHint}
import org.scalatest.matchers.should.Matchers

class Oas20UniquePlatformUnitValidationsTest extends UniquePlatformReportGenTest with Matchers {

  override val basePath: String    = "file://amf-cli/shared/src/test/resources/validations/oas2/"
  override val reportsPath: String = "amf-cli/shared/src/test/resources/validations/reports/oas2/"
  override val hint: Hint          = Oas20YamlHint

  test("missing schema in body parameter") {
    validate("missing-schema-body-parameter.json", Some("missing-schema-body-parameter.report"), Oas20Profile)
  }

  test("Oas path uri is invalid") {
    validate("invalid-endpoint-path-still-parses.json", Some("invalid-endpoint-path-still-parses.report"), Oas20Profile)
  }

  test("parameter of type array must have items property") {
    validate("missing-items-field.json", Some("missing-items-field.report"), Oas20Profile)
  }

  test("invalid fields when security type is basic") {
    validate("security/invalid-fields-basic-type.json", Some("invalid-fields-basic-type.report"), Oas20Profile)
  }

  test("Oauth2 security scheme missing flow and scopes required fields") {
    validate("security/missing-oauth2-fields.json", Some("missing-oauth2-fields.report"), Oas20Profile)
  }

  test("missing tokenUrl in Oauth2 flow") {
    validate("security/missing-tokenUrl-oauth2.json", Some("missing-tokenUrl-oauth2.report"), Oas20Profile)
  }

  test("missing authorizationUrl in Oauth2 flow") {
    validate(
      "security/missing-authorizationUrl-oauth2.json",
      Some("missing-authorizationUrl-oauth2.report"),
      Oas20Profile
    )
  }

  test("invalid Oauth2 flow fields") {
    validate("security/invalid-oauth2-fields.json", Some("invalid-oauth2-fields.report"), Oas20Profile)
  }

  test("Invalid security scheme type") {
    validate("security/invalid-security-scheme-type.json", Some("invalid-security-scheme-type.report"), Oas20Profile)
  }

  test("apiKey security type missing in and name fields") {
    validate("security/invalid-apikey-type.json", Some("invalid-apikey-type.report"), Oas20Profile)
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

  test("JSON with duplicate keys") {
    validate("duplicate-keys.json", Some("duplicate-keys.report"), Oas20Profile, overridedHint = Some(Oas20JsonHint))
  }

  test("invalid 'example' field in parameter object") {
    validate("invalid-example-field.json", Some("invalid-example-field.report"), Oas20Profile)
  }

  test("Validate ref key in operation object") {
    validate("invalid-ref-key-operation.json", Some("invalid-ref-key-operation.report"), Oas20Profile)
  }

  test("Validate oas20 with a baseUriParameters annotation") {
    validate("base-uri-annotation.json", Some("base-uri-annotation.report"), Oas20Profile)
  }
}
