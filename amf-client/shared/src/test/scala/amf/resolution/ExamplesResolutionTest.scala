package amf.resolution

import amf.core.emitter.RenderOptions
import amf.core.remote.{Amf, OasJsonHint, OasYamlHint, RamlYamlHint}

/**
  *
  */
class ExamplesResolutionTest extends ResolutionTest {
  override val basePath      = "amf-client/shared/src/test/resources/resolution/examples/"
  val validationPath: String = "amf-client/shared/src/test/resources/validations/"

  multiGoldenTest("Response examples oas to AMF", "response-examples.json.%s") { config =>
    cycle("response-examples.json",
          config.golden,
          OasJsonHint,
          target = Amf,
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Response declarations with multiple media types oas to AMF",
                  "response-declarations-with-multiple-media-types.%s") { config =>
    cycle("response-declarations-with-multiple-media-types.yaml",
          config.golden,
          OasYamlHint,
          target = Amf,
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Response examples raml to AMF", "response-examples.raml.%s") { config =>
    cycle("response-examples.raml",
          config.golden,
          RamlYamlHint,
          target = Amf,
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Typed external fragment as included example - Vocabulary", "examples/vocabulary-fragment/api.%s") {
    config =>
      cycle("examples/vocabulary-fragment/api.raml",
            config.golden,
            RamlYamlHint,
            target = Amf,
            directory = validationPath,
            renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Typed external fragment as included example - Dialect", "examples/dialect-fragment/api.%s") {
    config =>
      cycle("examples/dialect-fragment/api.raml",
            config.golden,
            RamlYamlHint,
            target = Amf,
            directory = validationPath,
            renderOptions = Some(config.renderOptions))
  }

  override def defaultRenderOptions: RenderOptions = RenderOptions().withSourceMaps.withPrettyPrint
}
