package amf.validation

import amf.core.remote.{Hint, OasYamlHint}

class OasYamlModelUniquePlatformReportTest extends UniquePlatformReportGenTest {

  override val basePath    = "file://amf-client/shared/src/test/resources/validations/"
  override val reportsPath = "amf-client/shared/src/test/resources/validations/reports/model/"

  test("Invalid recursion definition with self-reference") {
    validate("oas-definition-self-ref.yaml", Some("oas-definition-self-ref.report"))
  }

  test("Invalid recursion definition with self-chained-reference") {
    validate("oas-definition-self-chained-ref.yaml", Some("oas-definition-self-chained-ref.report"))
  }

  test("Avoid YMap error exception in parameters parser") {
    validate("ymap-exception/api.yaml", Some("ymap-exception.report"))
  }

  override val hint: Hint = OasYamlHint
}
