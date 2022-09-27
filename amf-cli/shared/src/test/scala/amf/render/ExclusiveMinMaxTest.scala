package amf.render
import amf.apicontract.client.scala.{AMFConfiguration, ConfigurationAdapter}
import amf.core.client.common.render.{JsonSchemaDraft4, JsonSchemaDraft7}
import amf.core.client.common.transform.PipelineId
import amf.core.client.scala.config.RenderOptions
import amf.core.internal.remote.JsonSchemaHint
import amf.resolution.ResolutionTest
import amf.shapes.client.scala.config.JsonSchemaConfiguration
import scala.concurrent.ExecutionContext

class ExclusiveMinMaxTest extends ResolutionTest {

  override val defaultPipeline: String                     = PipelineId.Editing
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global
  override val basePath: String                            = "amf-cli/shared/src/test/resources/render/exclusive-minimum-maximum/"
  val config: AMFConfiguration           = ConfigurationAdapter.adapt(JsonSchemaConfiguration.JsonSchema())
  val draft4RenderOptions: RenderOptions = RenderOptions().withSchemaVersion(JsonSchemaDraft4)
  val draft7RenderOptions: RenderOptions = RenderOptions().withSchemaVersion(JsonSchemaDraft7)

  // In Draft6 and higher exclusiveMinimum and exclusiveMaximum are numbers
  test("Parse Draft4(boolean) and render to Draft4(boolean)") {
    cycle(
      "exclusive-min-max-boolean.json",
      "exclusive-min-max-boolean.json",
      JsonSchemaHint,
      target = JsonSchemaHint,
      basePath,
      amfConfig = Option(config.withRenderOptions(draft4RenderOptions))
    )
  }

  test("Parse Draft4(boolean) and render to Draft7(numeric)") {
    cycle(
      "exclusive-min-max-boolean.json",
      "exclusive-min-max-boolean-to-draft7.json",
      JsonSchemaHint,
      target = JsonSchemaHint,
      basePath,
      amfConfig = Option(config.withRenderOptions(draft7RenderOptions))
    )
  }

  test("Parse Draft7(numeric) and render to Draft7(numeric)") {
    cycle(
      "exclusive-min-max-numeric.json",
      "exclusive-min-max-numeric.json",
      JsonSchemaHint,
      target = JsonSchemaHint,
      basePath,
      amfConfig = Option(config.withRenderOptions(draft7RenderOptions))
    )
  }

  test("Parse Draft7(numeric) and render to Draft4(boolean) with minimum defined") {
    cycle(
      "exclusive-min-max-numeric.json",
      "exclusive-min-max-numeric-to-draft4.json",
      JsonSchemaHint,
      target = JsonSchemaHint,
      basePath,
      amfConfig = Option(config.withRenderOptions(draft4RenderOptions))
    )
  }

  test("Parse Draft7(numeric) and render to Draft4(boolean) with minimum undefined") {
    cycle(
      "exclusive-min-max-numeric-no-minimum.json",
      "exclusive-min-max-numeric-no-minimum-to-draft4.json",
      JsonSchemaHint,
      target = JsonSchemaHint,
      basePath,
      amfConfig = Option(config.withRenderOptions(draft4RenderOptions))
    )
  }
}
