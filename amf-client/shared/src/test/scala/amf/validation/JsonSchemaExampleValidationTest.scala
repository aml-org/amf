package amf.validation

import amf.core.remote.{Hint, RamlYamlHint}

class JsonSchemaExampleValidationTest extends ValidationReportGenTest {
  override val basePath = "file://amf-client/shared/src/test/resources/validations/jsonschema/"

  override val reportsPath: String = "amf-client/shared/src/test/resources/validations/reports/jsonschema-examples/"
  test("JSON Schema allOf test1") {
    validatePlatform("allOf/api1.raml", Some("allOf-api1.report"))
  }

  test("JSON Schema allOf test2") {
    validatePlatform("allOf/api2.raml", Some("allOf-api2.report"))
  }

  test("JSON Schema allOf test3") {
    validatePlatform("allOf/api3.raml", Some("allOf-api3.report"))
  }

  test("JSON Schema anyOf test1") {
    validatePlatform("/anyOf/api1.raml", Some("anyOf-api1.report"))
  }

  test("JSON Schema anyOf test2") {
    validatePlatform("/anyOf/api2.raml", Some("anyOf-api2.report"))
  }

  test("JSON Schema anyOf test3") {
    validatePlatform("/anyOf/api3.raml", Some("anyOf-api3.report"))
  }

  test("JSON Schema oneOf test1") {
    validatePlatform("/oneOf/api1.raml", Some("oneOf-api1.report"))
  }

  test("JSON Schema oneOf test2") {
    validatePlatform("/oneOf/api2.raml", Some("oneOf-api2.report"))
  }

  test("JSON Schema oneOf test3") {
    validatePlatform("/oneOf/api3.raml", Some("oneOf-api3.report"))
  }

  test("JSON Schema not test1") {
    validatePlatform("/not/api1.raml", Some("not-api1.report"))
  }

  test("JSON Schema not test2") {
    validatePlatform("/not/api2.raml", Some("not-api2.report"))
  }

  test("JSON Schema not test3") {
    validatePlatform("/not/api3.raml", Some("not-api3.report"))
  }

  test("JSON Schema not test4") {
    validatePlatform("/not/api4.raml", Some("not-api4.report"))
  }

  ignore("JSON Schema ref test1") {
    validatePlatform("/ref/api1.raml", Some("ref-api1.report"))
  }

  ignore("JSON Schema ref test2") {
    validatePlatform("/ref/api2.raml", Some("ref-api2.report"))
  }

  ignore("JSON Schema ref test3") {
    validatePlatform("/ref/api3.raml", Some("ref-api3.report"))
  }

  ignore("JSON Schema ref test4") {
    validatePlatform("/ref/api4.raml", Some("ref-api4.report"))
  }

  ignore("JSON Schema ref test5") {
    validatePlatform("/ref/api5.raml", Some("ref-api5.report"))
  }

  ignore("JSON Schema ref test6") {
    validatePlatform("/ref/api6.raml", Some("ref-api6.report"))
  }
  override val hint: Hint = RamlYamlHint
}
