package amf.validation
import amf.Oas30Profile
import amf.core.remote.{Hint, OasYamlHint}

class Oas3ValidationTest extends UniquePlatformReportGenTest {
  override val basePath: String    = "file://amf-client/shared/src/test/resources/validations/oas3/"
  override val reportsPath: String = "amf-client/shared/src/test/resources/validations/reports/oas3/"
  override val hint: Hint          = OasYamlHint

  test("'Paths' property is required") {
    validate("paths-property.json", Some("paths-property.report"), Oas30Profile)
  }
}
