package amf.validation

import amf.RAML08Profile
import amf.core.remote.{Hint, RamlYamlHint}

class ValidApiExamplesValidationTest extends ValidModelTest {

  override val reportsPath: String = "amf-client/shared/src/test/resources/validations/reports/examples/"

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
    checkValid("/08/empty-payload-with-example.raml", RAML08Profile)
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

  test("Date times examples test") {
    checkValid("examples/date_time_validations.raml")
  }

  test("Test declared type with two uses adding example") {
    validatePlatform("/examples/declared-type-ref-add-example.raml", golden = Some("declared-type-ref-add-example.report"))
  }

  test("Test validate declared type with two uses") {
    validatePlatform("/examples/declared-type-ref.raml", golden = Some("declared-type-ref.report"))
  }

  test("Test valid string hierarchy examples") {
    validatePlatform("/examples/string-hierarchy.raml", golden = Some("string-hierarchy.report"))
  }

  test("Test valid api with pattern properties") {
    validatePlatform("/production/pattern_properties.raml", golden = Some("production_pattern_properties.report"))
  }

  test("Test valid api with type problems 1") {
    validatePlatform("/production/type_problems1.raml", golden = Some("type_problems1.report"))
  }

  ignore("Test valid api with type problems 2") {
    validatePlatform("/production/type_problems2/api.raml", golden = Some("type_problems2.report"))
  }

  test("Test valid api with type problems 3") {
    validatePlatform("/production/type_problems3.raml", golden = Some("type_problems3.report"))
  }

  override val hint: Hint = RamlYamlHint
}
