package amf.validation

import amf.OasProfile
import amf.core.remote.{Hint, OasJsonHint}

class OasExamplesValidationTest extends MultiPlatformReportGenTest {

  override val basePath: String    = "file://amf-client/shared/src/test/resources/validations/"
  override val reportsPath: String = "amf-client/shared/src/test/resources/validations/reports/examples/"

  test("Test examples in oas") {
    validate("/examples/examples-in-oas.json", Some("examples-in-oas.report"), OasProfile)
  }

  override val hint: Hint = OasJsonHint
}
