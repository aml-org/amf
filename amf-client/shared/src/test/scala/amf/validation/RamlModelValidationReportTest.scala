package amf.validation

import amf.core.remote.{Hint, RamlYamlHint}

class RamlModelValidationReportTest extends ValidationReportGenTest {

  override val basePath    = "file://amf-client/shared/src/test/resources/validations/"
  override val reportsPath = "amf-client/shared/src/test/resources/validations/reports/model/"

  test("Load dialect") {
    validate("data/error1.raml", Some("load-dialect-error1.report"))
  }

  test("Library example validation") {
    validate("library/nested.raml", Some("library-nested.report"))
  }

  // this should be in RamlPArserErrorTest but there is a lot a violations, so, its easier put in here
  test("Closed shapes validation") {
    validate("closed_nodes/api.raml", Some("closed-nodes.report"))
  }

  test("No title validation") {
    validate("webapi/no_title.raml", Some("webapi-no-title.report"))
  }

  //this is from resolution its ok here o i should add another test apart.

  test("Property overwriting") {
    validate("types/property_overwriting.raml", Some("property_overwriting.report"))
  }

  test("Invalid media type") {
    validate("webapi/invalid_media_type.raml", Some("invalid-media-type.report"))
  }

  test("json schema inheritance") {
    validate("types/schema_inheritance.raml", Some("schema_inheritance.report"))
  }

  test("xml schema inheritance") {
    validate("types/schema_inheritance2.raml", Some("schema_inheritance2.report"))
  }

  // Test that the library works ok or that there are some recursive ??
  test("Library with includes") {
    validate("library/with-include/api.raml", Some("library-includes-api.report"))
  }

  test("Max length validation") {
    validate("shapes/max-length.raml", Some("max-length.report"))
  }

  test("Min length validation") {
    validate("shapes/min-length.raml", Some("min-length.report"))
  }

  test("Exclusive example vs examples validation") {
    validate("facets/example_examples.raml", Some("example-examples.report"))
  }

  test("Exclusive queryString vs queryParameters validation") {
    validate("operation/query_string_parameters.raml", Some("query_string_parameters.report"))
  }

  test("float numeric constraints") {
    validate("/shapes/floats.raml", Some("shapes-floats.report"))
  }

  test("Invalid example no media types") {
    validate("/examples/example-no-media-type.raml", Some("example-no-media-type.report"))
  }

  test("Test out of range status code") {
    validate("/webapi/invalid_status_code.raml", Some("invalid-status-code.report"))
  }

  test("Test empty string in title") {
    validate("/webapi/invalid_title1.raml", Some("empty-title.report"))
  }

  test("Mandatory RAML documentation properties test") {
    validate("/documentation/api.raml", Some("documentation-api.report"))
  }

  test("Test minimum maximum constraint between facets") {
    validate("/facets/min-max-between.raml", Some("min-max-between.report"))
  }

  test("Test minItems maxItems constraint between facets") {
    validate("/facets/min-max-items-between.raml", Some("min-max-items-between.report"))
  }

  test("Test minLength maxLength constraint between facets") {
    validate("/facets/min-max-length-between.raml", Some("min-max-length-between.report"))
  }

  test("Test optional node implemented without var") {
    validate("/resource_types/optional-node-implemented.raml", Some("optional-node-implemented.report"))
  }

  override val hint: Hint = RamlYamlHint
}
