package amf

import amf.shapes.test.JsonSchemaBasedSpecSourceSpecTestBase

class LLMMetadataSourceSpecTest extends JsonSchemaBasedSpecSourceSpecTestBase {
  override def testConfig = LLMMetadataTestConfig.config
}
