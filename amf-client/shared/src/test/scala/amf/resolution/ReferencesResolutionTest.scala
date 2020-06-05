package amf.resolution

import amf.core.remote.{Amf, Oas20, OasYamlHint, RamlYamlHint}
import amf.core.resolution.pipelines.ResolutionPipeline

class ReferencesResolutionTest extends ResolutionTest {
  override val basePath: String = "amf-client/shared/src/test/resources/upanddown/"
  val resolutionPath: String    = "amf-client/shared/src/test/resources/resolution/"

  test("References resolution") {
    cycle("with_references.raml", "with_references_resolved.jsonld", RamlYamlHint, Amf)
  }

  test("Oas direct type alias - All objects case - Default pipeline") {
    cycle("all-objects-case.yaml",
          "all-objects-case.jsonld",
          OasYamlHint,
          Amf,
          transformWith = Some(Oas20),
          directory = s"$resolutionPath/oas-link-of-link/")
  }

  test("Oas direct type alias - Array in the middle case - Default pipeline") {
    cycle("array-in-the-middle.yaml",
          "array-in-the-middle.jsonld",
          OasYamlHint,
          Amf,
          transformWith = Some(Oas20),
          directory = s"$resolutionPath/oas-link-of-link/")
  }

  test("Oas direct type alias - Array with child recursion case - Default pipeline") {
    cycle(
      "array-with-child-recursion.yaml",
      "array-with-child-recursion.jsonld",
      OasYamlHint,
      Amf,
      transformWith = Some(Oas20),
      directory = s"$resolutionPath/oas-link-of-link/"
    )
  }
}
