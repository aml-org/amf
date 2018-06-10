package amf.validation

class ValidModelParserTest extends ValidModelTest {
  test("Valid baseUri validations test") {
    cycle("webapi/valid_baseuri.raml")
  }

  test("Example validation of a resource type") {
    cycle("webapi/valid_baseuri.raml")
  }
}
