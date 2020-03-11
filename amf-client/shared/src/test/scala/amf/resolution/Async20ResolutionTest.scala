package amf.resolution

import amf.core.emitter.RenderOptions
import amf.core.errorhandling.UnhandledErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.remote.AsyncYamlHint
import amf.core.remote.Vendor._
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.emit.AMFRenderer
import amf.io.FunSuiteCycleTests
import amf.plugins.document.webapi.Async20Plugin

import scala.concurrent.Future

class Async20ResolutionTest extends FunSuiteCycleTests {
  override def basePath: String = "amf-client/shared/src/test/resources/resolution/async20/"

  test("Message examples are propagated to payload shapes") {
    cycle("message-example-propagation.yaml", "message-example-propagation.jsonld", AsyncYamlHint, AMF)
  }

  test("defaultContentType overrides message contentType") {
    cycle("content-type-override.yaml", "content-type-override.jsonld", AsyncYamlHint, AMF)
  }

  test("Message traits are merged to message") {
    cycle("message-trait-merging.yaml", "message-trait-merging.jsonld", AsyncYamlHint, AMF)
  }

  test("Operation traits are merged to operation") {
    cycle("operation-trait-merging.yaml", "operation-trait-merging.jsonld", AsyncYamlHint, AMF)
  }

  override def transform(unit: BaseUnit, config: CycleConfig): BaseUnit =
    Async20Plugin.resolve(unit, UnhandledErrorHandler, ResolutionPipeline.EDITING_PIPELINE)

  override def render(unit: BaseUnit, config: CycleConfig, useAmfJsonldSerialization: Boolean): Future[String] = {
    new AMFRenderer(unit,
                    config.target,
                    RenderOptions().withSourceMaps.withRawSourceMaps.withCompactUris.withPrettyPrint,
                    config.syntax).renderToString
  }
}
