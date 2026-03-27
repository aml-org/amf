package amf

import amf.shapes.test.JsonSchemaBasedSpecSchemaLoaderTestBase

class AgenticNetworkSchemaLoaderTest extends JsonSchemaBasedSpecSchemaLoaderTestBase {
  override def testConfig = AgenticNetworkTestConfig.config
}
