package amf

import amf.shapes.test.JsonSchemaBasedSpecSourceSpecTestBase

class AgenticNetworkSourceSpecTest extends JsonSchemaBasedSpecSourceSpecTestBase {
  override def testConfig = AgenticNetworkTestConfig.config
}
