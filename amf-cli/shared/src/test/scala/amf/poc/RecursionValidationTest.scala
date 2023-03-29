package amf.poc
import amf.core.client.common.validation.AmfProfile
import amf.core.internal.remote.{AmfJsonHint, Hint, Raml10YamlHint}
import amf.validation.ResolutionForUniquePlatformReportTest

class RecursionValidationTest extends ResolutionForUniquePlatformReportTest {
  override val basePath: String    = "file://amf-cli/shared/src/test/resources/poc/"
  override val hint: Hint          = AmfJsonHint
  override val reportsPath: String = "file://amf-cli/shared/src/test/resources/poc/"

  test("Valid - Optional Property") {
    checkReport("recursion-valid-optional-property.raml", Some("recursion-valid-optional-property.report"))
  }

  test("Valid - Optional Property Complex") {
    checkReport("recursion-invalid-optional-property-complex.raml", Some("recursion-invalid-optional-property-complex.report"))
  }

  test("Invalid - Mandatory Property") {
    checkReport("recursion-invalid-mandatory-property.raml", Some("recursion-invalid-mandatory-property.report"))
  }

  test("Valid - minItems = 0") {
    checkReport("recursion-valid-minitems-zero.raml", Some("recursion-valid-minitems-zero.report"))
  }

  test("Invalid - minItems > 0") {
    checkReport("recursion-invalid-minitems-no-zero.raml", Some("recursion-invalid-minitems-no-zero.report"))
  }

  test("Valid - AdditionalProperties") {
    checkReport("recursion-valid-additionalproperties.yaml", Some("recursion-valid-additionalproperties.report"))
  }

  test("Valid - Optional Type") {
    checkReport("recursion-valid-optional-type.raml", Some("recursion-valid-optional-type.report"))
  }

  test("Invalid - Mandatory Type") {
    checkReport("recursion-invalid-mandatory-type.raml", Some("recursion-invalid-mandatory-type.report"))
  }

  test("Invalid: Inheritance Cycle") {
    checkReport("recursion-inheritance-cycle.raml", Some("recursion-inheritance-cycle.report"))
  }

  test("Valid: Simple Union") {
    checkReport("recursion-valid-union.raml", Some("recursion-valid-union.report"))
  }

  test("Invalid: Simple Union") {
    checkReport("recursion-invalid-union.raml", Some("recursion-invalid-union.report"))
  }

  test("Invalid: Nested Union") {
    checkReport("recursion-invalid-union-nested.raml", Some("recursion-invalid-union-nested.report"))
  }

  test("Valid: Nested Union (exit at first union)") {
    checkReport("recursion-valid-union-nested-exit-at-first.raml", Some("recursion-valid-union-nested-exit-at-first.report"))
  }

  test("Valid: Nested Union (exit at second union)") {
    checkReport("recursion-valid-union-nested-exit-at-second.raml", Some("recursion-valid-union-nested-exit-at-second.report"))
  }

  test("Valid: recursion in inherited property") {
    checkReport(
      "recursion-in-inherited-property.raml",
      Some("recursion-in-inherited-property.report")
    )
  }

  test("Cyclic reference in oneOf") {
    checkReport(
      "cyclic-reference-in-oneof.yaml",
      Some("cyclic-reference-in-oneof.report")
    )
  }

  test("Cyclic reference in property range") {
    checkReport(
      "cyclic-reference-in-property-range.yaml",
      Some("cyclic-reference-in-property-range.report")
    )
  }
}
