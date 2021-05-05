package amf.resolution

import amf.core.emitter.RenderOptions
import amf.core.remote.{Amf, Oas20JsonHint, Oas20YamlHint, Raml10YamlHint}

/**
  *
  */
class ExamplesResolutionTest extends ResolutionTest {
  override val basePath      = "amf-client/shared/src/test/resources/resolution/examples/"
  val validationPath: String = "amf-client/shared/src/test/resources/validations/"

  multiGoldenTest("Response examples oas to AMF", "response-examples.json.%s") { config =>
    cycle("response-examples.json",
          config.golden,
          Oas20JsonHint,
          target = Amf,
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Response declarations with multiple media types oas to AMF",
                  "response-declarations-with-multiple-media-types.%s") { config =>
    cycle("response-declarations-with-multiple-media-types.yaml",
          config.golden,
          Oas20YamlHint,
          target = Amf,
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Response examples raml to AMF", "response-examples.raml.%s") { config =>
    cycle("response-examples.raml",
          config.golden,
          Raml10YamlHint,
          target = Amf,
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Typed external fragment as included example - Vocabulary", "examples/vocabulary-fragment/api.%s") {
    config =>
      cycle("examples/vocabulary-fragment/api.raml",
            config.golden,
            Raml10YamlHint,
            target = Amf,
            directory = validationPath,
            renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Typed external fragment as included example - Dialect", "examples/dialect-fragment/api.%s") {
    config =>
      cycle("examples/dialect-fragment/api.raml",
            config.golden,
            Raml10YamlHint,
            target = Amf,
            directory = validationPath,
            renderOptions = Some(config.renderOptions))
  }

  override def defaultRenderOptions: RenderOptions = RenderOptions().withSourceMaps.withPrettyPrint
}
