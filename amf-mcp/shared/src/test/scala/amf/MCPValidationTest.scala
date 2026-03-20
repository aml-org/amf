package amf

import amf.shapes.test.JsonSchemaBasedSpecValidationTestBase

class MCPValidationTest extends JsonSchemaBasedSpecValidationTestBase {
  override def testConfig = MCPTestConfig.config
}
