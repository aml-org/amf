package amf

import amf.agentnetworkmetadata.client.scala.AgentNetworkMetadataConfiguration
import amf.agentnetworkmetadata.internal.plugins.parse.schema.AgentNetworkMetadataSchemaLoader
import amf.core.internal.remote.Spec
import amf.shapes.test._

class AgentNetworkMetadataCycleTest extends JsonSchemaBasedSpecCycleTestBase {
  override def testConfig: JsonSchemaBasedSpecTestConfig = AgentNetworkMetadataTestConfig.config
}

object AgentNetworkMetadataTestConfig {
  val config: JsonSchemaBasedSpecTestConfig = JsonSchemaBasedSpecTestConfig(
    specName     = "AgentNetworkMetadata",
    basePath     = "amf-agent-network-metadata/shared/src/test/resources/instances/",
    configuration = AgentNetworkMetadataConfiguration.AgentNetworkMetadata(),
    schemaLoader = AgentNetworkMetadataSchemaLoader,
    spec         = Spec.AGENT_NETWORK_METADATA,
    validInstances = Seq("valid/asset.json"),
    invalidInstances = Seq(InvalidInstance("invalid/asset.json", Some(1))),
    cycleInstances = Seq(CycleInstance("valid/asset.json", "valid/asset.jsonld"))
  )
}
