package amf.validation

class ValidOasYamlModelParserTest extends ValidModelTest {

  // W-18551455
  test("OAS $ref should support json-pointers to external file") {
    checkValid("oas3/operation-ref/api-external.yaml")
  }

  // W-18551468
  test("oas 30 external docs should work at schema level") {
    checkValid("/oas3/external-docs-in-schema.yaml")
  }

  test("Test multiple formData parameters") {
    checkValid("/parameters/multiple-formdata.yaml")
  }

  test("Integer response code") {
    checkValid("/response/integer-response-code.yaml")
  }

  test("Hack in pattern facet to validate correctly in jvm and js") {
    checkValid("/pattern/pattern-with-hack.yaml")
  }

  test("Recursive shape in additional properties doesnt raise violation") {
    checkValid("/recursives/oas/additional-properties.json")
  }

}
