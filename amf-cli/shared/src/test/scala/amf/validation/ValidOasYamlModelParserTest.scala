package amf.validation

import amf.core.internal.remote.{Hint, Oas20YamlHint}

class ValidOasYamlModelParserTest extends ValidModelTest {

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

  override val hint: Hint = Oas20YamlHint
}
