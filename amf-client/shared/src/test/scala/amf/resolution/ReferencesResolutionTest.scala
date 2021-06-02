package amf.resolution

import amf.client.remod.amfcore.config.RenderOptions
import amf.core.remote.{Amf, Oas20, Oas20YamlHint, Raml10, Raml10YamlHint}

class ReferencesResolutionTest extends ResolutionTest {
  override val basePath: String = "amf-client/shared/src/test/resources/upanddown/"
  val resolutionPath: String    = "amf-client/shared/src/test/resources/resolution/"

  multiGoldenTest("References resolution", "with_references_resolved.%s") { config =>
    cycle("with_references.raml",
          config.golden,
          Raml10YamlHint,
          target = Amf,
          renderOptions = Some(config.renderOptions),
          transformWith = Some(Raml10))
  }

  multiGoldenTest("Oas direct type alias - All objects case - Default pipeline", "all-objects-case.%s") { config =>
    cycle(
      "all-objects-case.yaml",
      config.golden,
      Oas20YamlHint,
      target = Amf,
      directory = s"$resolutionPath/oas-link-of-link/",
      transformWith = Some(Oas20),
      renderOptions = Some(config.renderOptions)
    )
  }

  multiGoldenTest("Oas direct type alias - Array in the middle case - Default pipeline", "array-in-the-middle.%s") {
    config =>
      cycle(
        "array-in-the-middle.yaml",
        config.golden,
        Oas20YamlHint,
        target = Amf,
        directory = s"$resolutionPath/oas-link-of-link/",
        transformWith = Some(Oas20),
        renderOptions = Some(config.renderOptions)
      )
  }

  multiGoldenTest("Oas direct type alias - Array with child recursion case - Default pipeline",
                  "array-with-child-recursion.%s") { config =>
    cycle(
      "array-with-child-recursion.yaml",
      config.golden,
      Oas20YamlHint,
      target = Amf,
      directory = s"$resolutionPath/oas-link-of-link/",
      transformWith = Some(Oas20),
      renderOptions = Some(config.renderOptions)
    )
  }

  override def defaultRenderOptions: RenderOptions = RenderOptions().withSourceMaps.withPrettyPrint
}
