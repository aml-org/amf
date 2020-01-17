package amf.validation
import amf.core.remote.{Hint, OasYamlHint}
import amf.{Oas20Profile, Oas30Profile}
import org.scalatest.Matchers

class UniquePlatformOasUnitValidationsTest extends UniquePlatformReportGenTest with Matchers {
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
}
