package amf.validation

import amf.Raml08Profile
import amf.core.remote.{Hint, RamlYamlHint}

class JsonSchemaExampleValidationTest extends MultiPlatformReportGenTest {
  override val basePath = "file://amf-client/shared/src/test/resources/validations/jsonschema/"

  override val reportsPath: String = "amf-client/shared/src/test/resources/validations/reports/jsonschema-examples/"
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

  // TODO: error messages are arriving in a non-deterministic order in the JVM. No golden file can be added
  ignore("JSON Schema oneOf test2") {
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

  test("JSON Schema ref test6") {
    validate("/ref/api6.raml", Some("ref-api6.report"))
  }

  test("JSON Schema ref test7") {
    validate("/ref/api7.raml", Some("ref-api7.report"))
  }

  test("JSON Schema ref test8") {
    validate("/ref/api8.raml", Some("ref-api8.report"))
  }

  test("Test validation with # in property shape name") {
    validate("/invalid-char-property-name.raml", None)
  }

  test("Exclusive Maximum Schema") {
    validate("/max-exclusive-schema.raml", Some("max-exclusive-schema.report"), profile = Raml08Profile)
  }

  test("Validate json schema with non url id.") {
    validate("/id-without-url/currencyapi.raml", None)
  }

  test("JSON Schema pattern properties") {
    validate("/jsonSchemaProperties.raml", Some("jsonSchemaProperties.report"))
  }

  test("JSON Schema Draft-3 required property support") {
    validate("/misc_shapes.raml", Some("misc_shapes.report"))
  }

  test("Examples JSON-Schema") {
    validate("/examples-json-schema.raml", None, profile = Raml08Profile)
  }

  test("JSON Schema enum not array") {
    validate("/enum-not-seq.raml", Some("enum-not-seq.report"))
  }

  test("JSON Schema array union") {
    validate("/array_unions.raml", Some("array_unions.report"))
  }

  test("JSON Schema array union 2") {
    validate("/APIMF-1173/api.raml")
  }

  test("JSON Schema path with spaces 1") {
    validate("/json-schema-space/api.raml")
  }
  test("JSON Schema path with spaces 2") {
    validate("/json-schema-space other/api.raml")
  }

  test("JSON Schema with valid datetime format") {
    validate("/date-time-format/api.raml")
  }

  test("JSON Schema 3 with valid boolean required") {
    validate("/required/schema3-boolean.raml")
  }

  test("JSON Schema 3 with invalid required array value") {
    validate("/required/schema3-array.raml", Some("/required/schema3-array.report"))
  }

  test("JSON Schema 4 with invalid required boolean value") {
    validate("/required/schema4-boolean.raml", Some("/required/schema4-boolean.report"))
  }

  test("JSON Schema 4 with valid required array value") {
    validate("/required/schema4-array.raml")
  }

  test("Unspecified JSON Schema with invalid required array value") {
    validate("/required/unspecified-array.raml")
  }

  test("Unspecified JSON Schema with invalid required boolean value") {
    validate("/required/unspecified-boolean.raml")
  }

  test("JSON Schema 4 property dependencies with unknown source property") {
    validate("/dependencies/property-dependencies.raml", Some("/dependencies/property-dependencies.report"))
  }

  test("JSON Schema 4 property dependencies with unknown target property") {
    validate("/dependencies/property-dependencies-unkown-target.raml",
             Some("/dependencies/property-dependencies-unknown-target.report"))
  }

  test("JSON Schema 4 schema dependencies with unknown source property") {
    validate("/dependencies/schema-dependencies.raml", Some("/dependencies/schema-dependencies.report"))
  }

  test("JSON Schema 7 type array") {
    validate("/type/type-array.raml")
  }

  override val hint: Hint = RamlYamlHint
}
