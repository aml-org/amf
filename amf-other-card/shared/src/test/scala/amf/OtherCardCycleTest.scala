package amf

import amf.core.internal.remote.Spec
import amf.othercard.client.scala.OtherCardConfiguration
import amf.othercard.internal.plugins.parse.schema.OtherCardSchemaLoader
import amf.shapes.test._

class OtherCardCycleTest extends JsonSchemaBasedSpecCycleTestBase {
  override def testConfig: JsonSchemaBasedSpecTestConfig = OtherCardTestConfig.config
}

object OtherCardTestConfig {
  val config: JsonSchemaBasedSpecTestConfig = JsonSchemaBasedSpecTestConfig(
    specName     = "OtherCard",
    basePath     = "amf-other-card/shared/src/test/resources/instances/",
    configuration = OtherCardConfiguration.OtherCard(),
    schemaLoader = OtherCardSchemaLoader,
    spec         = Spec.OTHER_CARD,
    validInstances = Seq("valid/asset.json"),
    invalidInstances = Seq(InvalidInstance("invalid/asset.json", Some(1))),
    cycleInstances = Seq(CycleInstance("valid/asset.json", "valid/asset.jsonld"))
  )
}
