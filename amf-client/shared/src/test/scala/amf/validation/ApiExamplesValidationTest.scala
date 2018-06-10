package amf.validation

class ApiExamplesValidationTest extends ValidationReportGenTest {

  override val basePath: String    = "file://amf-client/shared/src/test/resources/validations/"
  override val reportsPath: String = "amf-client/shared/src/test/resources/validations/reports/examples/"

  test("Array minCount 1") {
    cycle("examples/arrayItems1.raml", Some("array-items1.report"))
  }

  test("Example model validation test") {
    cycle("examples/examples_validation.raml", Some("examples_validation.report"))
  }

  // todo: review test ofor warning schema
  test("Validates html example value example1.raml") {
    cycle("examples/example1.raml", Some("example1.report"))
  }

  test("Discriminator test 1") {
    cycle("examples/discriminator1.raml", Some("discriminator1.report"))
  }

  test("Discriminator test 2") {
    cycle("examples/discriminator2.raml", Some("discriminator2.report"))
  }

  test("Shape facets model validations test") {
    cycle("facets/custom-facets.raml", Some("custom-facets.report"))
  }

  test("Annotations validations test") {
    cycle("annotations/annotations.raml", Some("annotations.report"))
  }

  test("Annotations enum validations test") {
    cycle("annotations/annotations_enum.raml", Some("annotations_enum.report"))
  }

  test("MinLength, maxlength facets validations test") {
    cycle("types/lengths.raml", Some("min-max-length.report"))
  }

  test("big numbers validations test") {
    cycle("types/big_nums.raml", Some("big_nums.report"))
  }

  test("Parse and validate named examples as external framents") {
    cycle("examples/inline-named-examples/api.raml", Some("inline-named-examples.report"))
  }

  test("Nil value validation") {
    cycle("examples/nil_validation.raml", Some("nil-validation.report"))
  }

  test("Example invalid min and max constraint validations test") {
    cycle("examples/invalid-max-min-constraint.raml", Some("invalid-max-min-constraint.report"))
  }

  test("Test js custom validation - multiple of") {
    cycle("custom-js-validations/mutiple-of.raml", Some("mutiple-of.report"))
  }
}
