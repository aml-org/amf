package amf

import amf.shapes.test.JsonSchemaBasedSpecSchemaLoaderTestBase

class AgentNetworkSchemaLoaderTest extends JsonSchemaBasedSpecSchemaLoaderTestBase {
  override def testConfig = AgentNetworkTestConfig.config
}
