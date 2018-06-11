package amf.validation

class ApiExamplesValidationTest extends ValidationReportGenTest {

  override val basePath: String    = "file://amf-client/shared/src/test/resources/validations/"
  override val reportsPath: String = "amf-client/shared/src/test/resources/validations/reports/examples/"

  test("Array minCount 1") {
    validate("examples/arrayItems1.raml", Some("array-items1.report"))
  }

  test("Example model validation test") {
    validate("examples/examples_validation.raml", Some("examples_validation.report"))
  }

  // todo: review test ofor warning schema
  test("Validates html example value example1.raml") {
    validate("examples/example1.raml", Some("example1.report"))
  }

  test("Discriminator test 1") {
    validate("examples/discriminator1.raml", Some("discriminator1.report"))
  }

  test("Discriminator test 2") {
    validate("examples/discriminator2.raml", Some("discriminator2.report"))
  }

  test("Shape facets model validations test") {
    validate("facets/custom-facets.raml", Some("custom-facets.report"))
  }

  test("Annotations validations test") {
    validate("annotations/annotations.raml", Some("annotations.report"))
  }

  test("Annotations enum validations test") {
    validate("annotations/annotations_enum.raml", Some("annotations_enum.report"))
  }

  test("MinLength, maxlength facets validations test") {
    validate("types/lengths.raml", Some("min-max-length.report"))
  }

  test("big numbers validations test") {
    validate("types/big_nums.raml", Some("big_nums.report"))
  }

  test("Parse and validate named examples as external framents") {
    validate("examples/inline-named-examples/api.raml", Some("inline-named-examples.report"))
  }

  test("Nil value validation") {
    validate("examples/nil_validation.raml", Some("nil-validation.report"))
  }

  test("Example invalid min and max constraint validations test") {
    validate("examples/invalid-max-min-constraint.raml", Some("invalid-max-min-constraint.report"))
  }

  test("Test js custom validation - multiple of") {
    validate("custom-js-validations/mutiple-of.raml", Some("mutiple-of.report"))
  }

  test("Can validate an array example") {
    validate("examples/invalid-property-in-array-items.raml", Some("invalid-property-in-array-items.report"))
//      assert(!report.conforms)
  }
}
