package amf.validation

import amf.core.remote.{Hint, OasJsonHint}

class OasModelValidationReportTest extends ValidationReportGenTest {

  override val basePath    = "file://parser-client/shared/src/test/resources/validations/"
  override val reportsPath = "parser-client/shared/src/test/resources/validations/reports/model/"

  test("Tags in oas") {
    validate("/webapi/tags.json", Some("webapi-tags.report"))
  }

  override val hint: Hint = OasJsonHint
}
