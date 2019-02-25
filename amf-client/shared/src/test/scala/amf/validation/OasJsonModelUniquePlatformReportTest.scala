package amf.validation

import amf.core.remote.{Hint, OasJsonHint}

class OasJsonModelUniquePlatformReportTest extends UniquePlatformReportGenTest {

  override val basePath    = "file://amf-client/shared/src/test/resources/validations/"
  override val reportsPath = "amf-client/shared/src/test/resources/validations/reports/model/"

  test("Tags in oas") {
    validate("/webapi/tags.json", Some("webapi-tags.report"))
  }

  test("Parse and validate invalid responses") {
    validate("invalid-status-code-string/api.json", Some("invalid-status-code-string-oas.report"))
  }

  test("Parameter without shape") {
    validate("parameter-without-shape/parameter-without-shape.json", Some("parameter-without-shape.report"))
  }

  test("Invalid required in oas schema") {
    validate("invalid-oas-required/invalid-oas-required.json", Some("invalid-oas-required.report"))
  }

  test("Warning when using raml security schemes") {
    validate("raml-security-in-oas.json", Some("raml-security-in-oas.report"))
  }

  test("Path parameter must have the property required defined") {
    validate("path-parameter-required/required-is-not-present.json", Some("required-is-not-present.report"))
  }

  test("Path parameters must have required set to true") {
    validate("path-parameter-required/required-set-to-false.json", Some("required-set-to-false.report"))
  }

  test("Operation ids are unique") {
    validate("duplicate-operation-ids.json", Some("duplicate-operation-ids.report"))
  }

  test("Read only property marked as required") {
    validate("read-only-property-marked-required.json", Some("read-only-property-marked-required.report"))
  }

  test("Duplicate parameters both defined inline") {
    validate("duplicate-parameters/duplicate-parameters.json", Some("duplicate-parameters.report"))
  }

  test("Duplicate parameters defined with reference") {
    validate("duplicate-parameters/duplicate-parameters-with-ref.json", Some("duplicate-parameters-with-ref.report"))
  }

  override val hint: Hint = OasJsonHint
}
