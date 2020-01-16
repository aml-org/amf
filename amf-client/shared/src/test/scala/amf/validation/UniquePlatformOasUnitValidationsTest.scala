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
    validate("invalid-endpoint-path-still-parses.json", Some("invalid-endpoint-path-still-parses.report"), Oas20Profile)
  }
}
