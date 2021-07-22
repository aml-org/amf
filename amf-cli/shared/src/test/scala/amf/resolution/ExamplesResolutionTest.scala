package amf.resolution

import amf.core.client.scala.config.RenderOptions
import amf.core.internal.remote._
import amf.testing.AmfJsonLd

/**
  *
  */
class ExamplesResolutionTest extends ResolutionTest {
  override val basePath      = "amf-cli/shared/src/test/resources/resolution/examples/"
  val validationPath: String = "amf-cli/shared/src/test/resources/validations/"

  multiGoldenTest("Response examples oas to AMF", "response-examples.json.%s") { config =>
    cycle("response-examples.json",
          config.golden,
          Oas20JsonHint,
          target = AmfJsonLd,
          renderOptions = Some(config.renderOptions),
          transformWith = Some(Raml10))
  }

  multiGoldenTest("Response declarations with multiple media types oas to AMF",
                  "response-declarations-with-multiple-media-types.%s") { config =>
    cycle(
      "response-declarations-with-multiple-media-types.yaml",
      config.golden,
      Oas20YamlHint,
      target = AmfJsonLd,
      renderOptions = Some(config.renderOptions),
      transformWith = Some(Raml10)
    )
  }

  multiGoldenTest("Response examples raml to AMF", "response-examples.raml.%s") { config =>
    cycle("response-examples.raml",
          config.golden,
          Raml10YamlHint,
          target = AmfJsonLd,
          renderOptions = Some(config.renderOptions),
          transformWith = Some(Raml10))
  }

  multiGoldenTest("Typed external fragment as included example - Vocabulary", "examples/vocabulary-fragment/api.%s") {
    config =>
      cycle(
        "examples/vocabulary-fragment/api.raml",
        config.golden,
        Raml10YamlHint,
        target = AmfJsonLd,
        directory = validationPath,
        renderOptions = Some(config.renderOptions),
        transformWith = Some(Raml10)
      )
  }

  multiGoldenTest("Typed external fragment as included example - Dialect", "examples/dialect-fragment/api.%s") {
    config =>
      cycle(
        "examples/dialect-fragment/api.raml",
        config.golden,
        Raml10YamlHint,
        target = AmfJsonLd,
        directory = validationPath,
        renderOptions = Some(config.renderOptions),
        transformWith = Some(Raml10)
      )
  }

  override def defaultRenderOptions: RenderOptions = RenderOptions().withSourceMaps.withPrettyPrint
}
