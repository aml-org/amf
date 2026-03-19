package amf

import amf.shapes.test.JsonSchemaBasedSpecSchemaLoaderTestBase

class AgentMetadataSchemaLoaderTest extends JsonSchemaBasedSpecSchemaLoaderTestBase {
  override def testConfig = AgentMetadataTestConfig.config
}
