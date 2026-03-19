package amf

import amf.shapes.test.JsonSchemaBasedSpecSchemaLoaderTestBase

class LLMMetadataSchemaLoaderTest extends JsonSchemaBasedSpecSchemaLoaderTestBase {
  override def testConfig = LLMMetadataTestConfig.config
}
