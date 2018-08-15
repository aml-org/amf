package amf.validation

import amf.OasProfile
import amf.core.remote.{Hint, OasJsonHint}

class ValidOasModelParserTest extends ValidModelTest {

  test("Shape with items in oas") {
    checkValid("/shapes/shape-with-items.json")
  }

  test("Test validate headers in request") {
    checkValid("/parameters/request-header.json", profile = OasProfile)
  }

  test("Test validate multiple tags") {
    checkValid("/multiple-tags.json", profile = OasProfile)
  }

  test("Test multiple formData parameters") {
    checkValid("/parameters/multiple-formdata.yaml", profile = OasProfile)
  }

  override val hint: Hint = OasJsonHint
}
