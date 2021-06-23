package amf.validation

import amf.core.client.common.validation.Oas20Profile
import amf.core.internal.remote.{Hint, Oas20YamlHint}

class Oas20MultiPlatformUnitValidationsTest extends MultiPlatformReportGenTest {
  override val basePath: String    = "file://amf-cli/shared/src/test/resources/validations/oas2/"
  override val reportsPath: String = "amf-cli/shared/src/test/resources/validations/reports/oas2/"
  override val hint: Hint          = Oas20YamlHint

  test("Missing file violation has location and position") {
    validate("missing-ref.yaml", Some("missing-ref.report"), Oas20Profile)
  }
}