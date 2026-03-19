package amf

import amf.agentcard.client.scala.AgentCardConfiguration
import amf.agentcard.internal.plugins.parse.schema.AgentCardSchemaLoader
import amf.core.internal.remote.Spec
import amf.shapes.test._

class AgentCardCycleTest extends JsonSchemaBasedSpecCycleTestBase {
  override def testConfig: JsonSchemaBasedSpecTestConfig = AgentCardTestConfig.config
}

object AgentCardTestConfig {
  val config: JsonSchemaBasedSpecTestConfig = JsonSchemaBasedSpecTestConfig(
    specName     = "AgentCard",
    basePath     = "amf-agent-card/shared/src/test/resources/instances/",
    configuration = AgentCardConfiguration.AgentCard(),
    schemaLoader = AgentCardSchemaLoader,
    spec         = Spec.AGENT_CARD,
    validInstances = Seq("valid/asset.json"),
    invalidInstances = Seq(InvalidInstance("invalid/asset.json", Some(7))),
    cycleInstances = Seq(CycleInstance("valid/asset.json", "valid/asset.jsonld"))
  )
}
