package amf.validation

import amf.OasProfile
import amf.core.remote.{Hint, OasJsonHint, OasYamlHint}

class ValidOasYamlModelParserTest extends ValidModelTest {

  test("Test multiple formData parameters") {
    checkValid("/parameters/multiple-formdata.yaml", OasProfile)
  }

  test("Integer response code") {
    checkValid("/response/integer-response-code.yaml", OasProfile)
  }

  test("Hack in pattern facet to validate correctly in jvm and js") {
    checkValid("/pattern/pattern-with-hack.yaml", OasProfile)
  }

  override val hint: Hint = OasYamlHint
}
