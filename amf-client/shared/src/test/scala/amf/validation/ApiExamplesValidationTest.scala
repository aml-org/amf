package amf.validation

class ApiExamplesValidationTest extends ValidationReportGenTest {

  override val basePath: String    = "file://amf-client/shared/src/test/resources/validations/examples/"
  override val reportsPath: String = "amf-client/shared/src/test/resources/validations/reports/examples/"

  test("Array minCount 1") {
    cycle("arrayItems1.raml", Some("array-items1.report"))
  }

  test("Array minCount 2") {
    cycle("arrayItems2.raml", None)
  }

  test("Example model validation test") {
    cycle("examples_validation.raml", Some("examples_validation.report"))
  }

  // todo: review test ofor warning schema
  test("Validates html example value example1.raml") {
    cycle("example1.raml", Some("example1.report"))
  }

  test("Discriminator test 1") {
    cycle("discriminator1.raml", Some("discriminator1.report"))
//      assert(!report.conforms)
//      assert(report.results.length == 3)
  }

  test("Discriminator test 2") {
    cycle("discriminator2.raml", Some("discriminator2.report"))
//      assert(!report.conforms)
//      assert(report.results.length == 1)
  }
}
