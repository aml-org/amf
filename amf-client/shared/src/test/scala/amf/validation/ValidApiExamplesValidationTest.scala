package amf.validation

class ValidApiExamplesValidationTest extends ValidModelTest {

  test("Example of object validations test") {
    cycle("examples/object-name-example.raml")
  }

  test("Example min and max constraint validations test") {
    cycle("examples/max-min-constraint.raml")
  }

  test("Array minCount 2") {
    cycle("examples/arrayItems2.raml", None)
  }
}
