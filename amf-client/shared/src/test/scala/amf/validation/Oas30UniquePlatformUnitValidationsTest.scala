package amf.validation
import amf.core.remote.{Hint, OasJsonHint, OasYamlHint}
import amf.{Oas20Profile, Oas30Profile}
import org.scalatest.Matchers

class Oas30UniquePlatformUnitValidationsTest extends UniquePlatformReportGenTest with Matchers {
  override val basePath: String    = "file://amf-client/shared/src/test/resources/validations/oas3/"
  override val reportsPath: String = "amf-client/shared/src/test/resources/validations/reports/oas3/"
  override val hint: Hint          = OasYamlHint

  test("'Paths' property is required") {
    validate("paths-property.json", Some("paths-property.report"), Oas30Profile)
  }

  test("Invalid facets in oauth2 security scheme") {
    validate("invalid-oauth-facets.json", Some("invalid-oauth-facets.report"), Oas30Profile)
  }

  test("Missing OAuth flow fields") {
    validate("missing-flow-fields.json", Some("missing-flow-fields.report"), Oas30Profile)
  }

  test("Invalid facets in OAuth 2 flow") {
    validate("invalid-facet-oauth2-flow.json", Some("invalid-facet-oauth2-flow.report"), Oas30Profile)
  }

  test("Missing fields in License object") {
    validate("missing-fields-in-license.json", Some("missing-fields-in-license.report"), Oas30Profile)
  }

  test("Null values in object fields") {
    validate("null-values-object-nodes.json", Some("null-values-object-nodes.report"), Oas30Profile)
  }

  test("Invalid style for parameter") {
    validate("invalid-style-for-parameter.json", Some("invalid-style-for-parameter.report"), Oas30Profile)
  }

  test("Invalid style for parameter2") {
    validate("invalid-style-for-parameter2.json", Some("invalid-style-for-parameter2.report"), Oas30Profile)
  }

  test("Tag with no name") {
    validate("tag-with-no-name.json", Some("tag-with-no-name.report"), Oas30Profile)
  }

  test("Tags has to be an array") {
    validate("tags-type-array.json", Some("tags-type-array.report"), Oas30Profile)
  }

  test("required cant have duplicate values") {
    validate("required-duplicate-values.json", Some("required-duplicate-values.report"), Oas30Profile)
  }

  test("required cant have duplicate values 2") {
    validate("required-duplicate-values2.json", Some("required-duplicate-values2.report"), Oas30Profile)
  }

  test("invalid query parameter schema") {
    validate("invalid-query-parameter-schema.json", Some("invalid-query-parameter-schema.report"), Oas30Profile)
  }

  test("invalid ref inside paths object") {
    validate("invalid-ref-inside-paths-object.json", Some("invalid-ref-inside-paths-object.report"), Oas30Profile)
  }

  test("Invalid ref with missing slash") {
    validate("invalid-ref-missing-slash.json", Some("invalid-ref-missing-slash.report"), Oas30Profile)
  }

  test("Nested callback external refs") {
    validate("nested-libraries/nested-callbacks/api.json", None, Oas30Profile)
  }

  test("Nested request external refs") {
    validate("nested-libraries/nested-requests/api.json", None, Oas30Profile)
  }

  test("Validate invalid parameter defined in request body link") {
    validate("param-in-request-link.yaml", Some("param-in-request-link.report"), Oas30Profile)
  }

  test("Nested yaml refs") {
    validate("nested-yaml-refs/api.yaml", None, Oas30Profile)
  }

  test("Invalid status code without quotes") {
    validate("invalid-status-code.yaml", Some("invalid-status-code.report"), Oas30Profile)
  }

  test("Invalid yaml tags") {
    validate("invalid-yaml-tags.yaml", Some("invalid-yaml-tags.report"), Oas30Profile)
  }

  test("Implicit timestamp invalid yaml tags") {
    validate("implicit-timestamp-yaml-tags.yaml", None, Oas30Profile)
  }

  test("Explicit timestamp invalid yaml tags") {
    validate("explicit-timestamp-invalid-yaml-tags.yaml",
             Some("explicit-timestamp-invalid-yaml-tags.report"),
             Oas30Profile)
  }

  test("Invalid header names according to RFC-7230") {
    validate("invalid-header-names.yaml", Some("invalid-header-names.report"), Oas30Profile)
  }

  test("JSON with duplicate keys") {
    validate("duplicate-keys.json", Some("duplicate-keys.report"), Oas30Profile, overridedHint = Some(OasJsonHint))
  }

  test("Valid $ref with array indices in pointer") {
    validate("ref-with-array-indices.json", None, Oas30Profile, overridedHint = Some(OasJsonHint))
  }

  test("'type' facet of types produces a violation") {
    validate("invalid-type-facet-as-map.yaml", Some("invalid-type-facet-as-map.report"), Oas30Profile)
  }

  test("invalid schema name as seq") {
    validate("invalid-schema-name-as-seq.yaml", Some("invalid-schema-name-as-seq.report"), Oas30Profile)
  }
}
