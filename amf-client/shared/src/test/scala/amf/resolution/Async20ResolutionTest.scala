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

  multiGoldenTest("Message examples are propagated to payload shapes", "message-example-propagation.%s") { config =>
    cycle("message-example-propagation.yaml",
          config.golden,
          AsyncYamlHint,
          target = AMF,
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("defaultContentType overrides message contentType", "content-type-override.%s") { config =>
    cycle("content-type-override.yaml",
          config.golden,
          AsyncYamlHint,
          target = AMF,
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Message traits are merged to message", "message-trait-merging.%s") { config =>
    cycle("message-trait-merging.yaml",
          config.golden,
          AsyncYamlHint,
          target = AMF,
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Operation traits are merged to operation", "operation-trait-merging.%s") { config =>
    cycle("operation-trait-merging.yaml",
          config.golden,
          AsyncYamlHint,
          target = AMF,
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Named parameter with reference to parameter in components", "named-parameter-with-ref.%s") {
    config =>
      cycle("named-parameter-with-ref.yaml",
            config.golden,
            AsyncYamlHint,
            target = AMF,
            renderOptions = Some(config.renderOptions))
  }

  override def transform(unit: BaseUnit, config: CycleConfig): BaseUnit =
    Async20Plugin.resolve(unit, UnhandledErrorHandler, ResolutionPipeline.EDITING_PIPELINE)

  override def defaultRenderOptions: RenderOptions =
    RenderOptions().withSourceMaps.withRawSourceMaps.withCompactUris.withPrettyPrint

  override def render(unit: BaseUnit, config: CycleConfig, options: RenderOptions): Future[String] = {
    new AMFRenderer(unit, config.target, options, config.syntax).renderToString
  }
}
