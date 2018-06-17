package amf.validation

import amf.core.remote.{Hint, OasJsonHint}

class OasModelValidationReportTest extends ValidationReportGenTest {

  override val basePath    = "file://amf-client/shared/src/test/resources/validations/"
  override val reportsPath = "amf-client/shared/src/test/resources/validations/reports/model/"

  test("Tags in oas") {
    validate("/webapi/tags.json", Some("webapi-tags.report"))
  }

  override val hint: Hint = OasJsonHint
}
