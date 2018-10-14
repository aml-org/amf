package amf.validation

import amf.OasProfile
import amf.core.remote.{Hint, OasJsonHint}

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

  override val hint: Hint = OasJsonHint
}
