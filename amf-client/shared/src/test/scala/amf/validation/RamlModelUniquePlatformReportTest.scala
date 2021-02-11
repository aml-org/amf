package amf.validation

import amf.{Raml08Profile, Raml10Profile}
import amf.core.remote.{Hint, RamlYamlHint}

class RamlModelUniquePlatformReportTest extends UniquePlatformReportGenTest {

  override val basePath    = "file://amf-client/shared/src/test/resources/validations/"
  override val reportsPath = "amf-client/shared/src/test/resources/validations/reports/model/"
  override val hint: Hint  = RamlYamlHint

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

  test("Property overwriting 2") {
    validate("types/property-overwriting-2.raml", Some("property-overwriting-2.report"))
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

  test("Invalid security scheme RAML 0.8") {
    validate("08/invalid-security.raml", Some("08/invalid-security.report"), profile = Raml08Profile)
  }

  test("Invalid security scheme RAML 1.0") {
    validate("invalid-security.raml", Some("invalid-security.report"), profile = Raml10Profile)
  }

  test("security scheme authorizationGrant RAML 1.0") {
    validate("/security-schemes/raml10AuthorizationGrant.raml", Some("invalid-auth-grant-10.report"))
  }

  test("security scheme authorizationGrant RAML 0.8") {
    validate("/security-schemes/raml08AuthorizationGrant.raml",
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
    validate("/security-schemes/invalid-key.raml", Some("invalid-key.report"))
  }

  test("Test null value in json when expecting scalar value") {
    validate("/null-value-json.raml", Some("null-value-json.report"))
  }

  test("Error when overriding file schema") {
    validate("file-schema-override/api.raml", Some("file-override-schema.report"))
  }

  test("Security schemes with empty type") {
    validate("security-schemes/empty-type.raml", Some("empty-type.report"))
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

  test("Multiple inheritance with contradicting restrictions defined inline") {
    validate("multiple-inheritance-restrictions.raml", Some("max-min-restriction.report"))
  }

  test("Invalid annotations in 08") {
    validate("invalid-annotations-08/invalid-annotations-08.raml", Some("invalid-annotations-08.report"))
  }

  test("Library closed shape") {
    validate("invalid-library/invalid-library.raml", Some("invalid-library.report"))
  }

  test("Library closed shape used in an api") {
    validate("invalid-library/api.raml", Some("invalid-library-used-in-api.report"))
  }

  test("Recursion in extension") {
    validate("recursion-in-extension/extension.raml", Some("recursion-in-extension.report"))
  }

  test("Recursion in extension 2") {
    validate("recursion-in-extension/extension.raml", Some("recursion-in-extension2.report"))
  }

  test("Defining library within types") {
    validate("invalid-references-to-library/using-types.raml", Some("include-library-using-types.report"))
  }

  test("Defining library in uses with include tag") {
    validate("invalid-references-to-library/using-uses.raml", Some("include-library-with-includes.report"))
  }

  test("Recursive inheritance case") {
    validate("complex-recursive-inheritance/lib.raml")
  }

  test("Including invalid fragment when NamedExample is expected") {
    validate("invalid-fragments/named-example-expected.raml", Some("including-invalid-datatype.report"))
  }

  test("Unresolved parameter in RAML 1.0 endpoint") {
    validate("unresolved-parameter.raml", Some("unresolved-parameter.report"))
  }

  test("Invalid protocols in root level") {
    validate("protocols/invalid-root-level-unkown-values.raml", Some("invalid-root-level-unkown-values.report"))
  }

  test("Protocols may not be defined as empty array") {
    validate("protocols/empty-protocols-root-and-method.raml", Some("invalid-empty-array.report"))
  }

  test("Method level protocol validations") {
    validate("protocols/invalid-method-level.raml", Some("invalid-method-level.report"))
  }

  test("Discriminator inheritance") {
    validate("discriminator/discriminator-inheritance.raml", Some("discriminator-inheritance.report"))
  }

  test("Discriminator basic behavior") {
    validate("discriminator/valid/basic-behavior.raml")
  }

  test("Unknown discriminator") {
    validate("discriminator/valid/unknown-discriminator.raml")
  }

  test("Validating general cases of allowed targets in annotations") {
    validate("annotations/allowed-targets/allowed-targets.raml", Some("allowed-target-annotations.report"))
  }

  test("Resource types and traits using annotations with allowed target") {
    validate("annotations/allowed-targets/resource-types-and-traits.raml",
             Some("allowed-target-resource-types-traits.report"))
  }

  test("Annotations with allowed target in extension and overlay") {
    validate("annotations/allowed-targets/overlay.raml", Some("allowed-target-overlay-extension.report"))
  }

  test("Missing discriminator property") {
    validate("discriminator/invalid/missing-discriminator-property.raml",
             Some("missing-discriminator-property.report"))
  }

  test("Invalid payload in RAML 08") {
    validate("08/invalid-payload.raml", Some("invalid-payload-08.report"))
  }

  test("JSON Schema false recursion") {
    validate("json-schema/api.raml")
  }

  // References in fragments within extension

  test("Reference in resource type fragment within extension") {
    validate("references-in-fragments-within-extension/rt-fragment/extension.raml")
  }

  test("Inexistent reference in resource type fragment within extension") {
    validate("references-in-fragments-within-extension/rt-fragment-non-existent/extension.raml")
  }

  test("Reference in trait fragment within extension") {
    validate("references-in-fragments-within-extension/trait-fragment/extension.raml")
  }

  test("Inexistent reference in trait fragment within extension") {
    validate("references-in-fragments-within-extension/trait-fragment-non-existent/extension.raml")
  }

  test("Reference in data type fragment within extension") {
    validate("references-in-fragments-within-extension/datatype-fragment/extension.raml")
  }

  test("Inexistent reference in data type fragment within extension") {
    validate(
      "references-in-fragments-within-extension/datatype-fragment-non-existent/extension.raml",
      Some("inexistent-reference-in-data-type-within-extension.report")
    )
  }

  test("Reference in fragment to extension declaration") {
    validate("references-in-fragments-within-extension/rt-fragment-extension-declaration/extension.raml")
  }

  test("maxLength and minLength negative values") {
    validate("facets/negative-max-min-length.raml", Some("negative-max-min-length.report"))
  }

  ignore("Yaml undefined anchor validation") {
    validate("yaml-alias.raml", Some("yaml-alias.report"))
  }

  test("Severity levels order in the report") {
    validate("severity-report-order/severity-report-order.raml", Some("severity-report-order.report"))
  }

  test("Invalid user defined facet names, and missing required facets") {
    validate("facets/invalid-custom-facets.raml", Some("invalid-custom-facets.report"))
  }

  test("Facet 'required' in type declarations") {
    validate("invalid-required-type-declaration.raml", Some("invalid-required-type-declaration.report"))
  }

  test("URI parameters do not allow '/' in default, enum, or example") {
    validate("uriparam-value-with-slash.raml", Some("uriparam-value-with-slash.report"))
  }

  test("Type datetime format with restricted values") {
    validate("datetime-format-values.raml", Some("datetime-format-values.report"))
  }

  test("Merging fields defined in resource type and endpoint") {
    validate("merging-resource-types/resource-type-merging-fields.raml", Some("resource-type-merging-fields.report"))
  }

  test("Resource Type - Plain text - In API") {
    validate("resource_types/plain-text/in-api/api.raml")
  }

  test("Resource Type - Plain text - In Extension 1") {
    validate("resource_types/plain-text/in-extension-A/extension.raml")
  }

  test("Resource Type - Plain text - In Extension 2") {
    validate("resource_types/plain-text/in-extension-B/extension.raml", Some("rt-plain-extension-invalid.report"))
  }

  test("Resource Type - Plain text - In Extension 3") {
    validate("resource_types/plain-text/in-extension-C/extension.raml")
  }

  test("Resource Type - Plain text - In Overlay") {
    validate("resource_types/plain-text/in-overlay/overlay.raml")
  }

  test("Resource Type - Plain text - In Fragment") {
    validate("resource_types/plain-text/in-fragment/api.raml")
  }

  test("Resource Type - Plain text - In Including File") {
    validate("resource_types/plain-text/in-including-file/api.raml")
  }

  test("Resource Type - Parameterized - Type") {
    validate("resource_types/parameterized/type/api.raml")
  }

  test("Resource Type - Parameterized - Library type") {
    validate("resource_types/parameterized/lib-type/api.raml")
  }

  test("Resource Type - Parameterized - Extension library type") {
    validate("resource_types/parameterized/extension-type/extension.raml")
  }

  test("Resource Type - Plain text - Test the independency of context between two rt") {
    validate("resource_types/plain-text/two-rt-unresolve-ref/api.raml", Some("two-rt-unresolve-ref.report"))
  }

  test("Resource Type - Plain text - Multiple libraries") {
    validate("resource_types/plain-text/multi-lib/api.raml")
  }

  test("Resource Type - Plain text - Libraries with same name in a merged context") {
    validate("resource_types/plain-text/multi-lib-same-name/api.raml")
  }

  test("JSON Schema relative references") {
    validate("json-schema-relative-references/api.raml")
  }

  test("Optional responses in traits with RAML 1.0") {
    validate("optional-responses/optional-responses-10.raml",
             Some("optional-responses-10.report"),
             profile = Raml10Profile)
  }

  test("Optional responses in traits with RAML 0.8") {
    validate("optional-responses/optional-responses-08.raml", profile = Raml08Profile)
  }

  test("Long number format is valid with number type in RAML 1.0") {
    validate("raml/valid-number-format.raml", None, profile = Raml10Profile)
  }

  test("Duplicate key defined in yaml") {
    validate("yaml-duplicate-key.raml", Some("yaml-duplicate-key.report"), profile = Raml10Profile)
  }

  test("Invalid recursion defined in trait and operation") {
    validate("invalid-recursion-in-trait.raml", Some("invalid-recursion-in-trait.report"), profile = Raml10Profile)
  }

  test("Invalid header names according to RFC-7230") {
    validate("invalid-header-names.raml", Some("invalid-header-names.report"), Raml10Profile)
  }

  test("Raml overlay with example without overloading type") {
    validate("overlays/overlay-with-example-overloading/overlay.raml", None, Raml10Profile)
  }

  // OAuth 1.0
  test("Missing requestTokenUri field in OAuth 1.0 security type") {
    validate("/raml/oauth1/missing-requestTokenUri-oauth1.raml", Some("missing-requestTokenUri-oauth1.report"))
  }

  test("Missing authorizationUri field in OAuth 1.0 security type") {
    validate("/raml/oauth1/missing-authorizationUri-oauth1.raml", Some("missing-authorizationUri-oauth1.report"))
  }

  test("Missing tokenCredentialsUri field in OAuth 1.0 security type") {
    validate("/raml/oauth1/missing-tokenCredentialsUri-oauth1.raml", Some("missing-tokenCredentialsUri-oauth1.report"))
  }

  // OAuth 2.0
  test("OAuth 2.0 security settings - authorization code") {
    validate("security-schemes/oauth-2/authorization-code.raml",
             Some("missing-authorization-code-fields.report"),
             Raml10Profile)
  }

  test("OAuth 2.0 security settings - client credentials grant") {
    validate("security-schemes/oauth-2/client-credentials.raml",
             Some("missing-client-credential-fields.report"),
             Raml10Profile)
  }

  test("OAuth 2.0 security settings - implicit grant") {
    validate("security-schemes/oauth-2/implicit.raml", Some("missing-implicit-fields.report"), Raml10Profile)
  }

  test("OAuth 2.0 security settings - password") {
    validate("security-schemes/oauth-2/password.raml", Some("missing-password-fields.report"), Raml10Profile)
  }

  test("OAuth 2.0 security settings - mulitple grants") {
    validate("security-schemes/oauth-2/multiple-grants.raml",
             Some("missing-fields-multiple-grants.report"),
             Raml10Profile)
  }

  test("OAuth 2.0 security settings - custom setting defined in securedBy facet") {
    validate("security-schemes/oauth-2/secured-by.raml", None, Raml10Profile)
  }

  test("Included JSON with duplicate keys") {
    validate("raml/included-json-with-duplicate-keys/api.raml",
             Some("raml/included-json-with-duplicate-keys.report"),
             Raml10Profile)
  }

  test("Documentation - title and description MUST be a non-empty string") {
    validate("raml/documentation-non-empty.raml", Some("raml/documentation-non-empty.report"), Raml10Profile)
  }

  test("Annotation types - check target locations are valid") {
    validate("raml/invalid-annotation-type.raml", Some("raml/invalid-annotation-type.report"), Raml10Profile)
  }

  test("RAML10 'version' as baseUriParameter is restricted if API version is Defined") {
    validate("raml/invalid-baseUriParameter-version.raml",
             Some("raml/invalid-baseUriParameter-version.report"),
             Raml10Profile)
  }

  test("RAML08 'version' as baseUriParameter is restricted if API version is Defined") {
    validate("08/invalid-baseUriParameter-version.raml",
             Some("08/invalid-baseUriParameter-version.report"),
             Raml08Profile)
  }

  test("RAML10 API must have 'version' if its present as variable in baseUri") {
    validate("raml/invalid-baseUri-version-variable.raml",
             Some("raml/invalid-baseUri-version-variable.report"),
             Raml10Profile)
  }

  test("RAML08 API must have 'version' if its present as variable in baseUri") {
    validate("08/invalid-baseUri-version-variable.raml",
             Some("08/invalid-baseUri-version-variable.report"),
             Raml08Profile)
  }

  test("Raml10 version baseUri variable valid definition") {
    validate("raml/valid-base-uri-version-variable.raml", None, Raml10Profile)
  }

  test("Raml08 version baseUri variable valid definition") {
    validate("08/valid-base-uri-version-variable.raml", None, Raml08Profile)
  }

  test("Invalid json example as violation") {
    validate("raml/included-example-invalid-json/api.raml",
             Some("included-example-invalid-json.report"),
             Raml08Profile)
  }

  test("Invalid json draft 7 with required boolean") {
    validate("raml/included-json-wrong-draft-key/api.raml",
             Some("included-json-wrong-draft-key.report"),
             Raml08Profile)
  }

  test("Draft 2019 unsupported facets for validation throw warning") {
    validate("raml/draft-2019/draft-2019-unsupported-facets-warning.raml",
             Some("draft-2019-unsupported-facets-warning.report"))
  }

  test("valid example defined nested within external fragment") {
    validate("raml/nested-example-external-fragment/api.raml", None, Raml08Profile)
  }

  test("RAML Library annotations") {
    validate("raml/library-annotations.raml", Some("library-annotations.report"))
  }

  test("Valid enum on union") {
    validate("valid-enum-on-union.raml", None, Raml10Profile)
  }
}
