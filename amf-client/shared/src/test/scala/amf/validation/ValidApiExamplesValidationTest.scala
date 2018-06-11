package amf.validation

class ValidApiExamplesValidationTest extends ValidModelTest {

  test("Example of object validations test") {
    checkValid("examples/object-name-example.raml")
  }

  test("Example min and max constraint validations test") {
    checkValid("examples/max-min-constraint.raml")
  }

  test("Array minCount 2") {
    checkValid("examples/arrayItems2.raml")
  }

  test("Force enum values as string in default string header") {
    checkValid("examples/force-enum-string.raml")
  }

  test("Minimun and maximun over float format") {
    checkValid("examples/min-max-float-format.raml")
  }
}
