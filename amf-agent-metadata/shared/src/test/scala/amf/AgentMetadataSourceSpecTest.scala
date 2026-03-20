package amf

import amf.shapes.test.JsonSchemaBasedSpecSourceSpecTestBase

class AgentMetadataSourceSpecTest extends JsonSchemaBasedSpecSourceSpecTestBase {
  override def testConfig = AgentMetadataTestConfig.config
}
