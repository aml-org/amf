package amf.validation

class ValidModelParserTest extends ValidModelTest {
  test("Valid baseUri validations test") {
    checkValid("webapi/valid_baseuri.raml")
  }

  test("Example validation of a resource type") {
    checkValid("webapi/valid_baseuri.raml")
  }
}
