package amf

import amf.agentnetwork.client.scala.AgentNetworkConfiguration
import amf.agentnetwork.internal.plugins.parse.schema.AgentNetworkSchemaLoader
import amf.core.internal.remote.Spec
import amf.shapes.test._

class AgentNetworkCycleTest extends JsonSchemaBasedSpecCycleTestBase {
  override def testConfig: JsonSchemaBasedSpecTestConfig = AgentNetworkTestConfig.config
}

object AgentNetworkTestConfig {
  val config: JsonSchemaBasedSpecTestConfig = JsonSchemaBasedSpecTestConfig(
    specName     = "AgentNetwork",
    basePath     = "amf-agent-network/shared/src/test/resources/instances/",
    configuration = AgentNetworkConfiguration.AgentNetwork(),
    schemaLoader = AgentNetworkSchemaLoader,
    spec         = Spec.AGENT_NETWORK,
    validInstances = Seq("valid/asset.yaml"),
    invalidInstances = Seq(InvalidInstance("invalid/asset.yaml", Some(1))),
    cycleInstances = Seq(CycleInstance("valid/asset.yaml", "valid/asset.jsonld"))
  )
}
