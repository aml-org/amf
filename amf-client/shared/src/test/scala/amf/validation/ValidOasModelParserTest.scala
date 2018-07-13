package amf.validation

import amf.OASProfile
import amf.core.remote.{Hint, OasJsonHint}

class ValidOasModelParserTest extends ValidModelTest {

  test("Shape with items in oas") {
    checkValid("/shapes/shape-with-items.json")
  }

  test("Test validate headers in request") {
    checkValid("/parameters/request-header.json", profile = OASProfile)
  }

  test("Test validate multiple tags") {
    checkValid("/multiple-tags.json", profile = OASProfile)
  }

  override val hint: Hint = OasJsonHint
}
