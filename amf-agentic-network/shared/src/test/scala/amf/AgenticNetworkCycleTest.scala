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
    specName     = "AgenticNetwork",
    basePath     = "amf-agentic-network/shared/src/test/resources/instances/",
    configuration = AgenticNetworkConfiguration.AgenticNetwork(),
    schemaLoader = AgenticNetworkSchemaLoader,
    spec         = Spec.AGENTIC_NETWORK,
    validInstances = Seq("valid/asset.yaml"),
    invalidInstances = Seq(InvalidInstance("invalid/asset.yaml", Some(1))),
    cycleInstances = Seq(CycleInstance("valid/asset.yaml", "valid/asset.jsonld"))
  )
}
