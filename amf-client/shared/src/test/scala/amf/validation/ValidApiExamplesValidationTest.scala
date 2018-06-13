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

  test("Enum with integer values") {
    checkValid("examples/enum-integer.raml")
  }

  test("Test validate pattern with valid example") {
    checkValid("examples/pattern-valid.raml")
  }

  test("Test validate union ex 1 with valid example a)") {
    checkValid("examples/union1a-valid.raml")
  }

  test("Test validate union ex 1 with valid example b)") {
    checkValid("examples/union1b-valid.raml")
  }

  test("Raml 0.8 Query Parameter Positive test case") {
    checkValid("/08/date-query-parameter-correct.raml")
  }
}
