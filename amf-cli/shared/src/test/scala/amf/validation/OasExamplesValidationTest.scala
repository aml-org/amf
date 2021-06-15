package amf.validation

import amf.core.client.common.validation.{Oas20Profile, Oas30Profile}
import amf.core.internal.remote.{Hint, Oas20JsonHint, Oas30JsonHint}

class OasExamplesValidationTest extends MultiPlatformReportGenTest {

  override val basePath: String    = "file://amf-cli/shared/src/test/resources/validations/"
  override val reportsPath: String = "amf-cli/shared/src/test/resources/validations/reports/examples/"

  test("Test examples in oas") {
    validate("/examples/examples-in-oas.json", Some("examples-in-oas.report"), Oas20Profile)
  }

  test("Validating examples defined in parameters and media types") {
    validate(
      "oas3/invalid-examples-params-and-media-type.json",
      Some("invalid-examples-params-and-media-type.report"),
      Oas30Profile,
      overridedHint = Some(Oas30JsonHint)
    )
  }

  test("Validating not constraint of schema object") {
    validate("oas3/not-constraint.json",
             Some("not-constraint.report"),
             Oas30Profile,
             overridedHint = Some(Oas30JsonHint))
  }

  test("additionalItems in OAS 3.0 schema") {
    validate("oas3/additional-items.json",
             Some("oas3-additional-items.report"),
             Oas30Profile,
             overridedHint = Some(Oas30JsonHint))
  }

  test("additionalItems in OAS 2.0 schema") {
    validate("/examples/additional-items.json", Some("oas2-additional-items.report"), Oas20Profile)
  }

  test("Multiple links ref in OAS 3.0") {
    validate("/oas3/multiple-links.json", None, Oas30Profile, overridedHint = Some(Oas30JsonHint))
  }

  override val hint: Hint = Oas20JsonHint
}
