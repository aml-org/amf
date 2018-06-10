package amf.validation

class JsonSchemaExampleValidationTest extends ValidationReportGenTest {
  override val basePath = "file://amf-client/shared/src/test/resources/validations/jsonschema/"

  override val reportsPath: String = "amf-client/shared/src/test/resources/validations/reports/jsonschema-examples/"
  test("JSON Schema allOf test1") {
    cycle("allOf/api1.raml", Some("allOf-api1.report"))
  }

  test("JSON Schema allOf test2") {
    cycle("allOf/api2.raml", Some("allOf-api2.report"))
  }

  test("JSON Schema allOf test3") {
    cycle("allOf/api3.raml", Some("allOf-api3.report"))
  }

  test("JSON Schema anyOf test1") {
    cycle("/anyOf/api1.raml", Some("anyOf-api1.report"))
  }

  test("JSON Schema anyOf test2") {
    cycle("/anyOf/api2.raml", Some("anyOf-api2.report"))
  }

  test("JSON Schema anyOf test3") {
    cycle("/anyOf/api3.raml", Some("anyOf-api3.report"))
  }

  test("JSON Schema oneOf test1") {
    cycle("/oneOf/api1.raml", Some("oneOf-api1.report"))
  }

  test("JSON Schema oneOf test2") {
    cycle("/oneOf/api2.raml", Some("oneOf-api2.report"))
  }

  test("JSON Schema oneOf test3") {
    cycle("/oneOf/api3.raml", Some("oneOf-api3.report"))
  }

  test("JSON Schema not test1") {
    cycle("/not/api1.raml", Some("not-api1.report"))
  }

  test("JSON Schema not test2") {
    cycle("/not/api2.raml", Some("not-api2.report"))
  }

  test("JSON Schema not test3") {
    cycle("/not/api3.raml", Some("not-api3.report"))
  }

  test("JSON Schema not test4") {
    cycle("/not/api4.raml", Some("not-api4.report"))
  }

  test("JSON Schema ref test1") {
    cycle("/ref/api1.raml", Some("ref-api1.report"))
  }

  test("JSON Schema ref test2") {
    cycle("/ref/api2.raml", Some("ref-api2.report"))
  }

  test("JSON Schema ref test3") {
    cycle("/ref/api3.raml", Some("ref-api3.report"))
  }

  test("JSON Schema ref test4") {
    cycle("/ref/api4.raml", Some("ref-api4.report"))
  }

  test("JSON Schema ref test5") {
    cycle("/ref/api5.raml", Some("ref-api5.report"))
  }

  ignore("JSON Schema ref test6") {
    cycle("/ref/api6.raml", Some("ref-api6.report"))
  }

}
