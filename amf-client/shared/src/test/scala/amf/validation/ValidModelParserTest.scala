package amf.validation

class ValidModelParserTest extends ValidModelTest {
  test("Valid baseUri validations test") {
    checkValid("webapi/valid_baseuri.raml")
  }

  test("Example validation of a resource type") {
    checkValid("webapi/valid_baseuri.raml")
  }

  test("Type inheritance with enum") {
    checkValid("types/enum-inheritance.raml")
  }

  test("External raml 0.8 fragment") {
    checkValid("08/external_fragment_test.raml")
  }
}
