package amf.validation

import amf.Async20Profile
import amf.core.remote.{AsyncYamlHint, Hint}

class ValidAsyncModelParserTest extends ValidModelTest {

  test("Full message object") {
    checkValid("../../upanddown/async20/message-obj.yaml", Async20Profile)
  }

  test("Draft 7 schemas") {
    checkValid("../../upanddown/async20/draft-7-schemas.yaml", Async20Profile)
  }

  test("Channel parameters") {
    checkValid("../../upanddown/async20/channel-parameters.yaml", Async20Profile)
  }

  test("Empty and dynamic binding") {
    checkValid("../../upanddown/async20/empty-dynamic-binding.yaml", Async20Profile)
  }

  override val basePath: String = "file://amf-client/shared/src/test/resources/validations/async/"
  override val hint: Hint       = AsyncYamlHint
}
