package amf.validation

import amf.ProfileNames
import amf.core.remote.{Hint, RamlYamlHint}

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

  test("Ignore empty example") {
    checkValid("/examples/empty-example.raml")
  }

  test("Empty payload with example validation") {
    checkValid("/08/empty-payload-with-example.raml", ProfileNames.RAML08)
  }

  test("Invalid yaml with scalar an map as value") {
    checkValid("/shapes/expanded-inheritance-with-example.raml")
  }

  test("Valid examples validation over union shapes") {
    checkValid("/shapes/examples-in-unions.raml")
  }

  test("Valid examples validation over union shapes 2") {
    checkValid("/shapes/unions_examples.raml")
  }

  test("Test validation of body with only example (default any shape)") {
    checkValid("/examples/only-example-body.raml")
  }

  test("Test seq in seq example") {
    checkValid("/examples/seq-in-seq.raml")
  }

  test("DateTimeOnly json example") {
    checkValid("examples/datetimeonly-json.raml")
  }

  test("Test test") {
    checkValid("examples/test.raml")
  }

  test("Date times examples test") {
    checkValid("examples/date_time_validations.raml")
  }

  override val hint: Hint = RamlYamlHint
}
