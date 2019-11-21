package amf.validation

import amf.{Oas30Profile, OasProfile}
import amf.core.remote.{Hint, OasJsonHint}

class OasExamplesValidationTest extends MultiPlatformReportGenTest {

  override val basePath: String    = "file://amf-client/shared/src/test/resources/validations/"
  override val reportsPath: String = "amf-client/shared/src/test/resources/validations/reports/examples/"

  test("Test examples in oas") {
    validate("/examples/examples-in-oas.json", Some("examples-in-oas.report"), OasProfile)
  }

  test("Validating examples defined in parameters and media types") {
    validate("oas3/invalid-examples-params-and-media-type.json",
             Some("invalid-examples-params-and-media-type.report"),
             Oas30Profile)
  }

  override val hint: Hint = OasJsonHint
}
