package amf

import amf.core.internal.remote.Spec
import amf.mcp.client.scala.MCPConfiguration
import amf.mcp.internal.plugins.parse.schema.MCPSchemaLoader
import amf.shapes.test._

class MCPCycleTest extends JsonSchemaBasedSpecCycleTestBase {
  override def testConfig: JsonSchemaBasedSpecTestConfig = MCPTestConfig.config
}

object MCPTestConfig {
  val config: JsonSchemaBasedSpecTestConfig = JsonSchemaBasedSpecTestConfig(
    specName     = "MCP",
    basePath     = "amf-mcp/shared/src/test/resources/instances/",
    configuration = MCPConfiguration.MCP(),
    schemaLoader = MCPSchemaLoader,
    spec         = Spec.MCP,
    validInstances = Seq("valid/asset.json"),
    invalidInstances = Seq(InvalidInstance("invalid/asset.json", Some(1))),
    cycleInstances = Seq(CycleInstance("valid/asset.json", "valid/asset.jsonld"))
  )
}
