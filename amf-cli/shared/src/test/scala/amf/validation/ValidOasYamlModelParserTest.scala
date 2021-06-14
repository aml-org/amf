package amf.validation

import amf.core.client.common.validation.Oas20Profile
import amf.core.internal.remote.{Hint, Oas20YamlHint}

class ValidOasYamlModelParserTest extends ValidModelTest {

  test("Test multiple formData parameters") {
    checkValid("/parameters/multiple-formdata.yaml", Oas20Profile)
  }

  test("Integer response code") {
    checkValid("/response/integer-response-code.yaml", Oas20Profile)
  }

  test("Hack in pattern facet to validate correctly in jvm and js") {
    checkValid("/pattern/pattern-with-hack.yaml", Oas20Profile)
  }

  test("Recursive shape in additional properties doesnt raise violation") {
    checkValid("/recursives/oas/additional-properties.json", Oas20Profile)
  }

  override val hint: Hint = Oas20YamlHint
}
