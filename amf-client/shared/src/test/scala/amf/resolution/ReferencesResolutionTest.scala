package amf.resolution

import amf.core.emitter.RenderOptions
import amf.core.remote.{Amf, Oas20, OasYamlHint, RamlYamlHint}

class ReferencesResolutionTest extends ResolutionTest {
  override val basePath: String = "amf-client/shared/src/test/resources/upanddown/"
  val resolutionPath: String    = "amf-client/shared/src/test/resources/resolution/"

  multiGoldenTest("References resolution", "with_references_resolved.%s") { config =>
    cycle("with_references.raml",
          config.golden,
          RamlYamlHint,
          target = Amf,
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Oas direct type alias - All objects case - Default pipeline", "all-objects-case.%s") { config =>
    cycle(
      "all-objects-case.yaml",
      config.golden,
      OasYamlHint,
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
        OasYamlHint,
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
      OasYamlHint,
      target = Amf,
      directory = s"$resolutionPath/oas-link-of-link/",
      transformWith = Some(Oas20),
      renderOptions = Some(config.renderOptions)
    )
  }

  override def defaultRenderOptions: RenderOptions = RenderOptions().withSourceMaps.withPrettyPrint
}
