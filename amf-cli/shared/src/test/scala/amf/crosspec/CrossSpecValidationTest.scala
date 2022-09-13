package amf.crosspec

import amf.validation.UniquePlatformReportGenTest

class CrossSpecValidationTest extends UniquePlatformReportGenTest {

  override val basePath: String    = "file://amf-cli/shared/src/test/resources/cross-spec/"
  override val reportsPath: String = "amf-cli/shared/src/test/resources/cross-spec/reports/"

  test("RAML 1.0 API with include of OAS 3.0 API") {
    validate("raml-point-oas/api-root.raml", Some("raml-point-oas.report"))
  }

  test("OAS 3.0 API with ref to OAS 2.0 API") {
    validate("oas3-point-oas2/api-root.yaml", Some("oas3-point-oas2.report"))
  }

  test("OAS 2.0 API with ref to OAS 3.0 Component Module") {
    validate("oas2-point-oas3component/api-root.json", Some("oas2-point-oas3component.report"))
  }

}
