package amf.validation

import amf.core.internal.remote.{Hint, Oas20JsonHint, Oas30JsonHint, Oas30YamlHint}
import org.scalatest.matchers.should.Matchers

class Oas30UniquePlatformUnitValidationsTest extends UniquePlatformReportGenTest with Matchers {
  override val basePath: String    = "file://amf-cli/shared/src/test/resources/validations/oas3/"
  override val reportsPath: String = "amf-cli/shared/src/test/resources/validations/reports/oas3/"
  val resolutionPath               = "file://amf-cli/shared/src/test/resources/resolution/"
  override val hint: Hint          = Oas30YamlHint

  test("'Paths' property is required") {
    validate("paths-property.json", Some("paths-property.report"))
  }

  test("Invalid facets in oauth2 security scheme") {
    validate("invalid-oauth-facets.json", Some("invalid-oauth-facets.report"))
  }

  test("Missing OAuth flow fields") {
    validate("missing-flow-fields.json", Some("missing-flow-fields.report"))
  }

  test("Invalid facets in OAuth 2 flow") {
    validate("invalid-facet-oauth2-flow.json", Some("invalid-facet-oauth2-flow.report"))
  }

  test("Missing fields in License object") {
    validate("missing-fields-in-license.json", Some("missing-fields-in-license.report"))
  }

  test("Null values in object fields") {
    validate("null-values-object-nodes.json", Some("null-values-object-nodes.report"))
  }

  test("Invalid style for parameter") {
    validate("invalid-style-for-parameter.json", Some("invalid-style-for-parameter.report"))
  }

  test("Invalid style for parameter2") {
    validate("invalid-style-for-parameter2.json", Some("invalid-style-for-parameter2.report"))
  }

  test("Tag with no name") {
    validate("tag-with-no-name.json", Some("tag-with-no-name.report"))
  }

  test("Tags has to be an array") {
    validate("tags-type-array.json", Some("tags-type-array.report"))
  }

  test("required cant have duplicate values") {
    validate("required-duplicate-values.json", Some("required-duplicate-values.report"))
  }

  test("required cant have duplicate values 2") {
    validate("required-duplicate-values2.json", Some("required-duplicate-values2.report"))
  }

  // todo: what does this source in here?
  test("invalid query parameter schema") {
    validate(
      "invalid-query-parameter-schema.json",
      Some("invalid-query-parameter-schema.report"),
      overridedHint = Some(Oas20JsonHint)
    )
  }

  test("invalid ref inside paths object") {
    validate("invalid-ref-inside-paths-object.json", Some("invalid-ref-inside-paths-object.report"))
  }

  test("Invalid ref with missing slash") {
    validate("invalid-ref-missing-slash.json", Some("invalid-ref-missing-slash.report"))
  }

  test("Nested callback external refs") {
    validate("nested-libraries/nested-callbacks/api.json")
  }

  test("Nested request external refs") {
    validate("nested-libraries/nested-requests/api.json")
  }

  test("Validate invalid parameter defined in request body link") {
    validate("param-in-request-link.yaml", Some("param-in-request-link.report"))
  }

  test("Nested yaml refs") {
    validate("nested-yaml-refs/api.yaml")
  }

  test("Invalid status code without quotes") {
    validate("invalid-status-code.yaml", Some("invalid-status-code.report"))
  }

  test("Invalid yaml tags") {
    validate("invalid-yaml-tags.yaml", Some("invalid-yaml-tags.report"))
  }

  test("Implicit timestamp invalid yaml tags") {
    validate("implicit-timestamp-yaml-tags.yaml")
  }

  test("Explicit timestamp invalid yaml tags") {
    validate("explicit-timestamp-invalid-yaml-tags.yaml", Some("explicit-timestamp-invalid-yaml-tags.report"))
  }

  test("Invalid header names according to RFC-7230") {
    validate("invalid-header-names.yaml", Some("invalid-header-names.report"))
  }

  test("JSON with duplicate keys") {
    validate("duplicate-keys.json", Some("duplicate-keys.report"), overridedHint = Some(Oas30JsonHint))
  }

  test("Valid $ref with array indices in pointer") {
    validate("ref-with-array-indices.json", None, overridedHint = Some(Oas30JsonHint))
  }

  test("'type' facet of types produces a violation") {
    validate("invalid-type-facet-as-map.yaml", Some("invalid-type-facet-as-map.report"))
  }

  test("invalid schema name as seq") {
    validate("invalid-schema-name-as-seq.yaml", Some("invalid-schema-name-as-seq.report"))
  }

  test("Validate ref key in operation object") {
    validate("invalid-ref-key-operation.json", Some("invalid-ref-key-operation.report"))
  }

  test("Unresolved value in discriminator mapping results in warning") {
    validate(
      "oas30-discriminator-invalid-mapping/api.yaml",
      Some("warning-unresolved-ref.report"),
      directory = resolutionPath
    )
  }

  test("Valid oas3 with inlined ref to schema with nullable") {
    validate("ref-nullable/ref-nullable-inlined.yaml")
  }

  test("Valid oas3 with external ref to schema with nullable") {
    validate("ref-nullable/ref-nullable-external.yaml")
  }

  test("Valid oas3 with ref to external unknown schema") {
    validate("ref-schema/ref-unknown-schema.json")
  }

  test("Valid oas3 with ref to external versioned schema") {
    validate("ref-schema/ref-version-schema.json")
  }

  test("Valid ref to endpoint with another ref") {
    validate("double-references/valid-ref-to-endpoint-with-ref.yaml")
  }

  test("Valid ref to header with another ref") {
    validate("double-references/valid-ref-to-header-with-ref.yaml")
  }

  test("Valid ref to parameter with another ref") {
    validate("double-references/valid-ref-to-endpoint-with-ref-to-param.yaml")
  }
}
