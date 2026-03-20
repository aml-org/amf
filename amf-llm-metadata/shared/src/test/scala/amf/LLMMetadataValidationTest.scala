package amf

import amf.shapes.test.JsonSchemaBasedSpecValidationTestBase

class LLMMetadataValidationTest extends JsonSchemaBasedSpecValidationTestBase {
  override def testConfig = LLMMetadataTestConfig.config
}
