package amf

import amf.agentmetadata.client.scala.AgentMetadataConfiguration
import amf.agentmetadata.internal.plugins.parse.schema.AgentMetadataSchemaLoader
import amf.core.internal.remote.Spec
import amf.shapes.test._

class AgentMetadataCycleTest extends JsonSchemaBasedSpecCycleTestBase {
  override def testConfig: JsonSchemaBasedSpecTestConfig = AgentMetadataTestConfig.config
}

object AgentMetadataTestConfig {
  val config: JsonSchemaBasedSpecTestConfig = JsonSchemaBasedSpecTestConfig(
    specName     = "AgentMetadata",
    basePath     = "amf-agent-metadata/shared/src/test/resources/instances/",
    configuration = AgentMetadataConfiguration.AgentMetadata(),
    schemaLoader = AgentMetadataSchemaLoader,
    spec         = Spec.AGENT_METADATA,
    validInstances = Seq("valid/asset.json"),
    invalidInstances = Seq(InvalidInstance("invalid/asset.json", Some(1))),
    cycleInstances = Seq(CycleInstance("valid/asset.json", "valid/asset.jsonld"))
  )
}
