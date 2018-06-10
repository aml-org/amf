package amf.validation

class ModelValidationReportTest extends ValidationReportGenTest {

  override val basePath    = "file://amf-client/shared/src/test/resources/validations/"
  override val reportsPath = "amf-client/shared/src/test/resources/validations/reports/model/"

  test("Load dialect") {
    cycle("data/error1.raml", Some("load-dialect-error1.report"))
  }

  test("Library example validation") {
    cycle("library/nested.raml", Some("library-nested.report"))
  }

  // this should be in RamlPArserErrorTest but there is a lot a violations, so, its easier put in here
  test("Closed shapes validation") {
    cycle("closed_nodes/api.raml", Some("closed-nodes.report"))
  }

}
