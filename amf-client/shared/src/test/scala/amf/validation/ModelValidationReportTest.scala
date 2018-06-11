package amf.validation

class ModelValidationReportTest extends ValidationReportGenTest {

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
}
