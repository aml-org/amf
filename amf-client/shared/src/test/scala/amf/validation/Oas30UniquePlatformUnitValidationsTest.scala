package amf.validation
import amf.core.remote.{Hint, OasYamlHint}
import amf.{Oas20Profile, Oas30Profile}
import org.scalatest.Matchers

class Oas30UniquePlatformUnitValidationsTest extends UniquePlatformReportGenTest with Matchers {
  override val basePath: String    = "file://amf-client/shared/src/test/resources/validations/oas3/"
  override val reportsPath: String = "amf-client/shared/src/test/resources/validations/reports/oas3/"
  override val hint: Hint          = OasYamlHint

  test("'Paths' property is required") {
    validate("paths-property.json", Some("paths-property.report"), Oas30Profile)
  }

  test("Oas path uri is invalid") {
    validate("invalid-endpoint-path-still-parses.json",
             Some("invalid-endpoint-path-still-parses.report"),
             Oas20Profile)
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
}
