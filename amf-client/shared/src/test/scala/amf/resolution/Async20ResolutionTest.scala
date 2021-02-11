package amf.resolution

import amf.core.emitter.RenderOptions
import amf.core.errorhandling.UnhandledErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.remote.{AsyncApi20, AsyncYamlHint, Vendor}
import amf.core.remote.Vendor._
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.emit.AMFRenderer
import amf.io.FunSuiteCycleTests
import amf.plugins.document.webapi.Async20Plugin

import scala.concurrent.Future

class Async20ResolutionTest extends ResolutionTest {
  override def basePath: String       = "amf-client/shared/src/test/resources/resolution/async20/"
  private val validationsPath: String = "amf-client/shared/src/test/resources/validations/async20/"

  multiGoldenTest("Message examples are propagated to payload and parameter shapes", "message-example-propagation.%s") {
    config =>
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

  multiGoldenTest("Message traits are merged to message and removed from extends",
                  "message-trait-merging-and-removed.%s") { config =>
    cycle(
      "message-trait-merging.yaml",
      config.golden,
      AsyncYamlHint,
      target = AMF,
      renderOptions = Some(config.renderOptions),
      pipeline = Some(ResolutionPipeline.DEFAULT_PIPELINE)
    )
  }

  multiGoldenTest("Operation traits are merged to operation and removed from extends",
                  "operation-trait-merging-and-removed.%s") { config =>
    cycle(
      "operation-trait-merging.yaml",
      config.golden,
      AsyncYamlHint,
      target = AMF,
      renderOptions = Some(config.renderOptions),
      pipeline = Some(ResolutionPipeline.DEFAULT_PIPELINE)
    )
  }

  multiGoldenTest("Named parameter with reference to parameter in components", "named-parameter-with-ref.%s") {
    config =>
      cycle("named-parameter-with-ref.yaml",
            config.golden,
            AsyncYamlHint,
            target = AMF,
            renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Reference to external raml data type fragment with includes at root of payload",
                  "include-root-payload.%s") { config =>
    cycle(
      "include-data-type-at-root-of-payload.yaml",
      config.golden,
      AsyncYamlHint,
      target = AMF,
      directory = validationsPath + "raml-data-type-references/",
      renderOptions = Some(config.renderOptions)
    )
  }

  multiGoldenTest("Reference to external raml data type fragment with chained reference", "chained-include.%s") {
    config =>
      cycle(
        "include-data-type-with-chained-reference.yaml",
        config.golden,
        AsyncYamlHint,
        target = AMF,
        directory = validationsPath + "raml-data-type-references/",
        renderOptions = Some(config.renderOptions)
      )
  }

  multiGoldenTest("Referencing raml content with $ref - data type fragment", "ref-data-type-fragment.%s") { config =>
    cycle(
      "ref-data-type-fragment.yaml",
      config.golden,
      AsyncYamlHint,
      target = AMF,
      directory = validationsPath + "raml-data-type-references/",
      renderOptions = Some(config.renderOptions)
    )
  }

  multiGoldenTest("Referencing raml content with $ref - type defined in library", "ref-type-in-library.%s") { config =>
    cycle(
      "ref-type-in-library.yaml",
      config.golden,
      AsyncYamlHint,
      target = AMF,
      directory = validationsPath + "raml-data-type-references/",
      renderOptions = Some(config.renderOptions)
    )
  }

  multiGoldenTest("Referencing raml content with $ref - type defined in external yaml", "ref-external-yaml.%s") {
    config =>
      cycle(
        "ref-external-yaml.yaml",
        config.golden,
        AsyncYamlHint,
        target = AMF,
        directory = validationsPath + "raml-data-type-references/",
        renderOptions = Some(config.renderOptions)
      )
  }

  multiGoldenTest("Verify isolated raml context in external data type fragment using ref",
                  "ref-data-type-fragment-invalid.%s") { config =>
    cycle(
      "ref-data-type-fragment-invalid.yaml",
      config.golden,
      AsyncYamlHint,
      target = AMF,
      directory = validationsPath + "raml-data-type-references/",
      renderOptions = Some(config.renderOptions)
    )
  }

  multiGoldenTest("Type forward referencing to check future declarations working correctly",
                  "type-forward-referencing.%s") { config =>
    cycle(
      "type-forward-referencing.yaml",
      config.golden,
      AsyncYamlHint,
      target = AMF,
      renderOptions = Some(config.renderOptions)
    )
  }

  multiGoldenTest("Referencing external message trait must have abstract field", "external-ref-message-trait.%s") {
    config =>
      cycle(
        "valid-external-ref-message-trait.yaml",
        config.golden,
        AsyncYamlHint,
        target = AMF,
        directory = validationsPath + "validations/external-reference/",
        renderOptions = Some(config.renderOptions)
      )
  }

  override val defaultVendor: Option[Vendor] = Some(AsyncApi20)
  override val defaultPipelineToUse: String  = ResolutionPipeline.EDITING_PIPELINE

  override def defaultRenderOptions: RenderOptions =
    RenderOptions().withSourceMaps.withRawSourceMaps.withCompactUris.withPrettyPrint

  override def render(unit: BaseUnit, config: CycleConfig, options: RenderOptions): Future[String] = {
    new AMFRenderer(unit, config.target, options, config.syntax).renderToString
  }
}
