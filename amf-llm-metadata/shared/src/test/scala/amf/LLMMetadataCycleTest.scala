package amf

import amf.core.internal.remote.Spec
import amf.llmmetadata.client.scala.LLMMetadataConfiguration
import amf.llmmetadata.internal.plugins.parse.schema.LLMMetadataSchemaLoader
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.test._

class LLMMetadataCycleTest extends JsonSchemaBasedSpecCycleTestBase {
  override def testConfig: JsonSchemaBasedSpecTestConfig = LLMMetadataTestConfig.config
}

object LLMMetadataTestConfig {
  val config: JsonSchemaBasedSpecTestConfig = JsonSchemaBasedSpecTestConfig(
    specName     = "LLMMetadata",
    basePath     = "amf-llm-metadata/shared/src/test/resources/instances/",
    configuration = LLMMetadataConfiguration.LLMMetadata(),
    schemaLoader = LLMMetadataSchemaLoader,
    spec         = Spec.LLM_METADATA,
    validInstances = Seq("valid/asset.json"),
    invalidInstances = Seq(InvalidInstance("invalid/asset.json", None)),
    cycleInstances = Seq(CycleInstance("valid/asset.json", "valid/asset.jsonld")),
    schemaShapeCheck = _.isInstanceOf[AnyShape]
  )
}
