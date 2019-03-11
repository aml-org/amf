package amf.validation

import amf.Raml08Profile
import amf.core.remote.{Hint, RamlYamlHint}

class RamlModelUniquePlatformReportTest extends UniquePlatformReportGenTest {

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

  test("Test overlay without extends") {
    validate("/extends/Overlay-Extension/overlay.raml", Some("overlay-without-extends.report"))
  }

  test("Test extension without extends") {
    validate("/extends/Overlay-Extension/extension.raml", Some("extension-without-extends.report"))
  }

  test("Test maxProperties and minProperties constraint between facets") {
    validate("/facets/min-max-properties-between.raml", Some("min-max-properties-between.report"))
  }

  test("Test variable not implemented in resource type use") {
    validate("/resource_types/variable-not-implemented-resourcetype.raml",
             Some("variable-not-implemented-resourcetype.report"))
  }

  test("Invalid security scheme") {
    validate("invalid-security.raml", Some("invalid-security.report"), profile = Raml08Profile)
  }

  test("security scheme authorizationGrant RAML 1.0") {
    validate("/securitySchemes/raml10AuthorizationGrant.raml", Some("invalid-auth-grant-10.report"))
  }

  test("security scheme authorizationGrant RAML 0.8") {
    validate("/securitySchemes/raml08AuthorizationGrant.raml",
             Some("invalid-auth-grant-08.report"),
             profile = Raml08Profile)

  }

  test("File type minLength/maxLength validation") {
    validate("/shapes/file-min-max-length.raml", Some("file-min-max-length.report"))
  }

  test("Invalid include library") {
    validate("/invalid-library-include/api.raml", Some("invalid-library-include.report"))
  }

  test("Invalid include security scheme in securedBy") {
    validate("/invalid-secured-by-include/api.raml", Some("invalid-secured-by-include.report"))
  }

  test("Invalid parameter link") {
    validate("/parameters/invalid-link.raml", Some("invalid-link.report"))
  }

  test("Payload with no mediaType") {
    validate("/payloads/no-media-type.raml", Some("no-media-type.report"))
  }

  test("Invalid security scheme key") {
    validate("/securitySchemes/invalid-key.raml", Some("invalid-key.report"))
  }

  test("Test null value in json when expecting scalar value") {
    validate("/null-value-json.raml", Some("null-value-json.report"))
  }

  test("Error when overriding file schema") {
    validate("file-schema-override/api.raml", Some("file-override-schema.report"))
  }

  test("Security schemes with empty type") {
    validate("securitySchemes/empty-type.raml", Some("empty-type.report"))
  }

  test("Extension with empty extends") {
    validate("extends/empty-extends.raml", Some("empty-extends.report"))
  }

  test("Parse and validate invalid responses") {
    validate("invalid-status-code-string/api.raml", Some("invalid-status-code-string-raml.report"))
  }

  test("Invalid array definition in enum") {
    validate("invalid-enum-array.raml", Some("invalid-enum-array.raml.report"))
  }

  test("Invalid Raml with json schema that refs path with spaces") {
    validate("raml-json-ref-with-spaces/api.raml", Some("raml-json-ref-with-spaces.report"))
  }

  test("Invalid json schema type") {
    validate("invalid-schema-type/invalid-schema-type.raml", Some("invalid-schema-type.report"))
  }

  test("Invalid reference with #") {
    validate("invalid-reference/api.raml", Some("invalid-reference.report"))
  }

  test("Invalid reference from overlay to swagger document") {
    validate("invalid-cross-overlay/invalid-cross-overlay.raml", Some("invalid-cross-overlay.report"))
  }

  test("Invalid xml wrapped scalar") {
    validate("invalidXmlWrappedScalar.raml", Some("invalidXmlWrappedScalar.report"))
  }

  test("Invalid xml attribute non scalar") {
    validate("invalidXmlAttributeNonScalar.raml", Some("invalidXmlAttributeNonScalar.report"))
  }

  test("NamedExample closed shape") {
    validate("named-example-closed-shape/named-example-closed-shape.raml", Some("named-example-closed-shape.report"))
  }

  test("Invalid resource type with closed shape") {
    validate("/resource_types/invalid/resource-type-closed-shape.raml", Some("resource-type-closed-shape.report"))
  }

  test("Invalid resource type with closed parametrized shape") {
    validate("/resource_types/invalid/resource-type-closed-parametrized-shape.raml",
             Some("resource-type-closed-parametrized-shape.report"))
  }

  test("Invalid trait with closed shape") {
    validate("/traits/invalid/trait-closed-shape.raml", Some("trait-closed-shape.report"))
  }

  test("Invalid trait with closed parametrized shape") {
    validate("/traits/invalid/trait-closed-parametrized-shape.raml", Some("trait-closed-parametrized-shape.report"))
  }

  // Missing parameters

  test("Missing parameter in trait key") {
    validate("/traits/invalid/trait-missing-parameter-in-key.raml", Some("trait-missing-parameter-in-key.report"))
  }

  test("Missing parameter in trait value") {
    validate("/traits/invalid/trait-missing-parameter-in-value.raml", Some("trait-missing-parameter-in-value.report"))
  }

  test("Missing parameter in resource type key") {
    validate("/resource_types/invalid/resource-type-missing-parameter-in-key.raml",
             Some("resource-type-missing-parameter-in-key.report"))
  }

  test("Missing parameter in resource type value") {
    validate("/resource_types/invalid/resource-type-missing-parameter-in-value.raml",
             Some("resource-type-missing-parameter-in-value.report"))
  }

  // Optional methods

  test("Closed shape in applied optional method") {
    validate(
      "/resource_types/invalid/optional-methods/resource-type-closed-shape-in-applied-optional-method.raml",
      Some("resource-type-closed-shape-in-applied-optional-method.report")
    )
  }

  test("Closed shape in unapplied optional method") {
    validate(
      "/resource_types/invalid/optional-methods/resource-type-closed-shape-in-unapplied-optional-method.raml",
      Some("resource-type-closed-shape-in-unapplied-optional-method.report")
    )
  }

  test("Closed parametrized shape in applied optional method") {
    validate(
      "/resource_types/invalid/optional-methods/resource-type-closed-parametrized-shape-in-applied-optional-method.raml",
      Some("resource-type-closed-parametrized-shape-in-applied-optional-method.report")
    )
  }

  test("Closed parametrized shape in unapplied optional method") {
    validate(
      "/resource_types/invalid/optional-methods/resource-type-closed-parametrized-shape-in-unapplied-optional-method.raml",
      Some("resource-type-closed-parametrized-shape-in-unapplied-optional-method.report")
    )
  }

  override val hint: Hint = RamlYamlHint
}
