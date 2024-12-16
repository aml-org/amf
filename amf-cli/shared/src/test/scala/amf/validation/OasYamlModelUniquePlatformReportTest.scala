package amf.validation

import amf.apicontract.client.scala.OASConfiguration

class OasYamlModelUniquePlatformReportTest extends UniquePlatformReportGenTest {

  override val basePath    = "file://amf-cli/shared/src/test/resources/validations/"
  override val reportsPath = "amf-cli/shared/src/test/resources/validations/reports/model/"
  val cyclePath: String = "file://amf-cli/shared/src/test/resources/upanddown/oas31/"

  test("Invalid recursion definition with self-reference") {
    validate("oas-definition-self-ref.yaml", Some("oas-definition-self-ref.report"))
  }

  test("Invalid recursion definition with self-chained-reference") {
    validate("oas-definition-self-chained-ref.yaml", Some("oas-definition-self-chained-ref.report"))
  }

  test("Avoid YMap error exception in parameters parser") {
    validate("ymap-exception/api.yaml", Some("ymap-exception.report"))
  }

  test("Avoid exception with ref to empty file") {
    validate("empty-file-ref-oas/api.yaml", Some("empty-file-ref-oas.report"))
  }

  test("OAS 3.1 full API should be valid") {
    validate("oas-31-full.yaml", None, configOverride = Some(OASConfiguration.OAS31()), directory = cyclePath)
  }

}
