package amf.validation

import amf.core.remote.{Hint, RamlYamlHint}

class JsonSchemaExampleValidationTest extends ValidationReportGenTest {
  override val basePath = "file://parser-client/shared/src/test/resources/validations/jsonschema/"

  override val reportsPath: String = "parser-client/shared/src/test/resources/validations/reports/jsonschema-examples/"
  test("JSON Schema allOf test1") {
    validate("allOf/api1.raml", Some("allOf-api1.report"))
  }

  test("JSON Schema allOf test2") {
    validate("allOf/api2.raml", Some("allOf-api2.report"))
  }

  test("JSON Schema allOf test3") {
    validate("allOf/api3.raml", Some("allOf-api3.report"))
  }

  test("JSON Schema anyOf test1") {
    validate("/anyOf/api1.raml", Some("anyOf-api1.report"))
  }

  test("JSON Schema anyOf test2") {
    validate("/anyOf/api2.raml", Some("anyOf-api2.report"))
  }

  test("JSON Schema anyOf test3") {
    validate("/anyOf/api3.raml", Some("anyOf-api3.report"))
  }

  test("JSON Schema oneOf test1") {
    validate("/oneOf/api1.raml", Some("oneOf-api1.report"))
  }

  test("JSON Schema oneOf test2") {
    validate("/oneOf/api2.raml", Some("oneOf-api2.report"))
  }

  test("JSON Schema oneOf test3") {
    validate("/oneOf/api3.raml", Some("oneOf-api3.report"))
  }

  test("JSON Schema not test1") {
    validate("/not/api1.raml", Some("not-api1.report"))
  }

  test("JSON Schema not test2") {
    validate("/not/api2.raml", Some("not-api2.report"))
  }

  test("JSON Schema not test3") {
    validate("/not/api3.raml", Some("not-api3.report"))
  }

  test("JSON Schema not test4") {
    validate("/not/api4.raml", Some("not-api4.report"))
  }

  test("JSON Schema ref test1") {
    validate("/ref/api1.raml", Some("ref-api1.report"))
  }

  test("JSON Schema ref test2") {
    validate("/ref/api2.raml", Some("ref-api2.report"))
  }

  test("JSON Schema ref test3") {
    validate("/ref/api3.raml", Some("ref-api3.report"))
  }

  test("JSON Schema ref test4") {
    validate("/ref/api4.raml", Some("ref-api4.report"))
  }

  test("JSON Schema ref test5") {
    validate("/ref/api5.raml", Some("ref-api5.report"))
  }

  ignore("JSON Schema ref test6") {
    validate("/ref/api6.raml", Some("ref-api6.report"))
  }
  override val hint: Hint = RamlYamlHint
}
