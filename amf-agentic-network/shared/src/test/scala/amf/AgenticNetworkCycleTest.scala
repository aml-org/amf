package amf

import amf.agenticnetwork.client.scala.AgenticNetworkConfiguration
import amf.agenticnetwork.internal.plugins.parse.schema.AgenticNetworkSchemaLoader
import amf.core.internal.remote.Spec
import amf.shapes.test._

class AgenticNetworkCycleTest extends JsonSchemaBasedSpecCycleTestBase {
  override def testConfig: JsonSchemaBasedSpecTestConfig = AgenticNetworkTestConfig.config
}

object AgenticNetworkTestConfig {
  val config: JsonSchemaBasedSpecTestConfig = JsonSchemaBasedSpecTestConfig(
      specName = "AgenticNetwork",
      basePath = "amf-agentic-network/shared/src/test/resources/instances/",
      configuration = AgenticNetworkConfiguration.AgenticNetwork(),
      schemaLoader = AgenticNetworkSchemaLoader,
      spec = Spec.AGENTIC_NETWORK,
      // valid/asset.yaml is a complete example, reference/valid/asset.yaml is a minimal example with a reference to a agent script
      validInstances = Seq("valid/asset.yaml", "reference/valid/asset.yaml"),
      invalidInstances = Seq(
          InvalidInstance("invalid/asset.yaml", Some(1), Seq("agentNetwork")), // This is a JSON Schema validation error, but in JVM and JS the message is different
          InvalidInstance("reference/invalid/asset.yaml", Some(1), Seq("reference/invalid/agent-script/agent1.agent")) // This is a FileNotFound error, but in JVM and JS the message is different
      ),
      cycleInstances = Seq(CycleInstance("valid/asset.yaml", "valid/asset.jsonld"),
                           CycleInstance("reference/valid/asset.yaml", "reference/valid/asset.jsonld"))
  )
}
