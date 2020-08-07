package amf.validation

import amf.Oas20Profile
import amf.core.remote.{Hint, OasYamlHint}

class Oas20MultiPlatformUnitValidationsTest extends MultiPlatformReportGenTest {
  override val basePath: String    = "file://amf-client/shared/src/test/resources/validations/oas2/"
  override val reportsPath: String = "amf-client/shared/src/test/resources/validations/reports/oas2/"
  override val hint: Hint          = OasYamlHint

  test("Missing file violation has location and position") {
    validate("missing-ref.yaml", Some("missing-ref.report"), Oas20Profile)
  }
}
