package amf.resolution

import amf.apicontract.client.scala.{AMFConfiguration, AsyncAPIConfiguration}
import amf.core.client.common.transform._
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.remote.{AmfJsonHint, Async20YamlHint, AsyncApi20, Vendor}

class Async20ResolutionTest extends ResolutionTest {
  override def basePath: String       = "amf-cli/shared/src/test/resources/resolution/async20/"
  private val validationsPath: String = "amf-cli/shared/src/test/resources/validations/async20/"

  multiGoldenTest("Message examples are propagated to payload and parameter shapes", "message-example-propagation.%s") {
    config =>
      cycle("message-example-propagation.yaml",
            config.golden,
            Async20YamlHint,
            target = AmfJsonHint,
            renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("defaultContentType overrides message contentType", "content-type-override.%s") { config =>
    cycle("content-type-override.yaml",
          config.golden,
          Async20YamlHint,
          target = AmfJsonHint,
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Message traits are merged to message", "message-trait-merging.%s") { config =>
    cycle("message-trait-merging.yaml",
          config.golden,
          Async20YamlHint,
          target = AmfJsonHint,
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Operation traits are merged to operation", "operation-trait-merging.%s") { config =>
    cycle("operation-trait-merging.yaml",
          config.golden,
          Async20YamlHint,
          target = AmfJsonHint,
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Message traits are merged to message and removed from extends",
                  "message-trait-merging-and-removed.%s") { config =>
    cycle(
      "message-trait-merging.yaml",
      config.golden,
      Async20YamlHint,
      target = AmfJsonHint,
      renderOptions = Some(config.renderOptions),
      pipeline = Some(PipelineId.Default)
    )
  }

  multiGoldenTest("Operation traits are merged to operation and removed from extends",
                  "operation-trait-merging-and-removed.%s") { config =>
    cycle(
      "operation-trait-merging.yaml",
      config.golden,
      Async20YamlHint,
      target = AmfJsonHint,
      renderOptions = Some(config.renderOptions),
      pipeline = Some(PipelineId.Default)
    )
  }

  multiGoldenTest("Named parameter with reference to parameter in components", "named-parameter-with-ref.%s") {
    config =>
      cycle("named-parameter-with-ref.yaml",
            config.golden,
            Async20YamlHint,
            target = AmfJsonHint,
            renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Reference to external raml data type fragment with includes at root of payload",
                  "include-root-payload.%s") { config =>
    cycle(
      "include-data-type-at-root-of-payload.yaml",
      config.golden,
      Async20YamlHint,
      target = AmfJsonHint,
      directory = validationsPath + "raml-data-type-references/",
      renderOptions = Some(config.renderOptions)
    )
  }

  multiGoldenTest("Reference to external raml data type fragment with chained reference", "chained-include.%s") {
    config =>
      cycle(
        "include-data-type-with-chained-reference.yaml",
        config.golden,
        Async20YamlHint,
        target = AmfJsonHint,
        directory = validationsPath + "raml-data-type-references/",
        renderOptions = Some(config.renderOptions)
      )
  }

  multiGoldenTest("Referencing raml content with $ref - data type fragment", "ref-data-type-fragment.%s") { config =>
    cycle(
      "ref-data-type-fragment.yaml",
      config.golden,
      Async20YamlHint,
      target = AmfJsonHint,
      directory = validationsPath + "raml-data-type-references/",
      renderOptions = Some(config.renderOptions)
    )
  }

  multiGoldenTest("Referencing raml content with $ref - type defined in library", "ref-type-in-library.%s") { config =>
    cycle(
      "ref-type-in-library.yaml",
      config.golden,
      Async20YamlHint,
      target = AmfJsonHint,
      directory = validationsPath + "raml-data-type-references/",
      renderOptions = Some(config.renderOptions)
    )
  }

  multiGoldenTest("Referencing raml content with $ref - type defined in external yaml", "ref-external-yaml.%s") {
    config =>
      cycle(
        "ref-external-yaml.yaml",
        config.golden,
        Async20YamlHint,
        target = AmfJsonHint,
        directory = validationsPath + "raml-data-type-references/",
        renderOptions = Some(config.renderOptions)
      )
  }

  multiGoldenTest("Verify isolated raml context in external data type fragment using ref",
                  "ref-data-type-fragment-invalid.%s") { config =>
    cycle(
      "ref-data-type-fragment-invalid.yaml",
      config.golden,
      Async20YamlHint,
      target = AmfJsonHint,
      directory = validationsPath + "raml-data-type-references/",
      renderOptions = Some(config.renderOptions)
    )
  }

  multiGoldenTest("Type forward referencing to check future declarations working correctly",
                  "type-forward-referencing.%s") { config =>
    cycle(
      "type-forward-referencing.yaml",
      config.golden,
      Async20YamlHint,
      target = AmfJsonHint,
      renderOptions = Some(config.renderOptions)
    )
  }

  multiGoldenTest("Referencing external message trait must have abstract field", "external-ref-message-trait.%s") {
    config =>
      cycle(
        "valid-external-ref-message-trait.yaml",
        config.golden,
        Async20YamlHint,
        target = AmfJsonHint,
        directory = validationsPath + "validations/external-reference/",
        renderOptions = Some(config.renderOptions)
      )
  }

  multiGoldenTest("Referencing external operation trait", "external-ref-operation-trait.%s") { config =>
    cycle(
      "valid-external-ref-operation-trait.yaml",
      config.golden,
      Async20YamlHint,
      target = AmfJsonHint,
      directory = validationsPath + "validations/external-reference/",
      renderOptions = Some(config.renderOptions)
    )
  }

  override def transform(unit: BaseUnit, config: CycleConfig, amfConfig: AMFConfiguration): BaseUnit = {
    super.transform(unit, config, AsyncAPIConfiguration.Async20())
  }

  override val defaultVendor: Option[Vendor] = Some(AsyncApi20)
  override val defaultPipeline: String       = PipelineId.Editing

  override def defaultRenderOptions: RenderOptions =
    RenderOptions().withSourceMaps.withRawSourceMaps.withCompactUris.withPrettyPrint
}
