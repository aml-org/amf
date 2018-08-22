package amf.validation

import amf.core.remote.{Hint, RamlYamlHint}
import amf.{AMFProfile, RAML08Profile}

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

  test("Minimum and maximum over float format") {
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

  test("Ignore empty default") {
    checkValid("/examples/empty-default.raml")
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

  test("Test valid string hierarchy examples") {
    checkValid("/examples/string-hierarchy.raml")
  }

  test("Test declared type with two uses adding example") {
    validate("/examples/declared-type-ref-add-example.raml", golden = Some("declared-type-ref-add-example.report"))
  }

  test("Test validate declared type with two uses") {
    validate("/examples/declared-type-ref.raml", golden = Some("declared-type-ref.report"))
  }

  test("Test invalid responses api") {
    validate("/production/responses-invalid.raml", golden = Some("production_responses_invalid.report"))
  }

  test("HERE_HERE Test invalid responses api (2)") {
    validate("/production/responses-invalid-2.raml", golden = Some("production_responses_invalid_2.report"))
  }

  test("Test valid api with pattern properties") {
    validate("/production/pattern_properties.raml", golden = Some("production_pattern_properties.report"))
  }

  test("Test valid api with type problems 1") {
    validate("/production/type_problems1.raml", golden = Some("type_problems1.report"))
  }

  ignore("Test valid api with type problems 2") {
    validate("/production/type_problems2/api.raml", golden = Some("type_problems2.report"))
  }

  test("Test valid api with type problems 3") {
    validate("/production/type_problems3.raml", golden = Some("type_problems3.report"))
  }

  test("Test api with duplicated null keys") {
    validate("/production/null-keys/api.raml", golden = Some("null-keys.report"))
  }

  test("Test rest connect example") {
    validate("/production/rest_connect/api.raml", golden = Some("rest_connect.report"))
  }

  test("Test rest connect bad example") {
    validate("/production/rest_connect/apibad.raml", golden = Some("rest_connect_bad.report"))
  }

  test("Test inheritance example") {
    validate("/production/inheritance.raml", golden = Some("inheritance.report"))
  }

  test("Test json schema inheritance") {
    validate("/production/json_schema_inheritance/api.raml", golden = Some("json_schema_inheritance.report"))
  }

  test("Valid type example 1 test") {
    validate("/examples/validex1.raml", profile = AMFProfile)
  }

  test("Valid type example 2 test") {
    validate("/examples/validex2.raml", profile = AMFProfile)
  }

  test("Test validate trait with quoted string example variable") {
    validate("/traits/trait-string-quoted-node.raml", profile = AMFProfile)
  }

  test("Test properties with special names") {
    validate("/property-names.raml", profile = AMFProfile)
  }

  test("Test enum number in string format validation") {
    checkValid("/examples/enum-number-string/api.raml")
  }

  test("Include twice same json schema and add example in raml 08") {
    checkValid("/examples/reuse-json-schema/api.raml", profile = RAML08Profile)
  }

  test("Date format not SYaml timestamp") {
    validate("/types/mhra-e-payment-v1.raml")
  }

  test("Test more than one variable with link node in trait") {
    validate("/traits/two-included-examples.raml")
  }

  test("Spec usage examples example validation") {
    validate("/examples/spec_examples_example.raml")
  }

  test("Test for different examples") {
    validate("/examples/examples.raml")
  }

  test("Check binary file included for any shape(ignore validation)") {
    validate("/examples/image-file-example/in-anyshape.raml")
  }

  // xml example in any shape types are no more candidates, so there is no warning
  test("Test unsupported example with raml08 profile") {
    validate("/examples/unsupported-examples-08.raml", profile = RAML08Profile)
  }

  test("Test json quoted string example") {
    validate("/examples/json-quoted-example.raml")
  }

  test("Test external include json example in trait var") {
    checkValid("/examples/json-frag-trait/api.raml")
  }

  test("Test empty examples entry value") {
    checkValid("/examples/empty-examples/api.raml")
  }

  test("Test valid object against enum") {
    checkValid("/enums/valid-objects-enums.raml")
  }

  test("Test valid array against enum") {
    checkValid("/enums/valid-array-enums.raml")
  }

  test("Test valid array of object against enum") {
    checkValid("/enums/valid-obj-array-enums.raml")
  }
  override val hint: Hint = RamlYamlHint
}
