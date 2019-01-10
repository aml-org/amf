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

  override val hint: Hint = OasJsonHint
}
