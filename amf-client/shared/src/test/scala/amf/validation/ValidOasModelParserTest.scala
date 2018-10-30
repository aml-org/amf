package amf.validation

import amf.OasProfile
import amf.core.remote.{Hint, OasJsonHint, RamlYamlHint}

class ValidOasModelParserTest extends ValidModelTest {

  test("Shape with items in oas") {
    checkValid("/shapes/shape-with-items.json", OasProfile)
  }

  test("Test validate headers in request") {
    checkValid("/parameters/request-header.json", OasProfile)
  }

  test("Test validate multiple tags") {
    checkValid("/multiple-tags.json", OasProfile)
  }

  test("Test multiple formData parameters") {
    checkValid("/parameters/multiple-formdata.yaml", OasProfile)
  }

  test("Integer response code") {
    checkValid("/response/integer-response-code.yaml", OasProfile)
  }

  test("In body binding param") {
    checkValid("/parameters/binding-body.json", OasProfile)
  }

  test("Valid media types") {
    checkValid("/payloads/valid-media-types.json", OasProfile)
  }

  test("Hack in pattern facet to validate correctly in jvm and js") {
    checkValid("/pattern/pattern-with-hack.yaml", OasProfile)
  }

  test("formData payload with ref") {
    checkValid("/payloads/form-data-with-ref.json", OasProfile)
  }

  override val hint: Hint = OasJsonHint
}
