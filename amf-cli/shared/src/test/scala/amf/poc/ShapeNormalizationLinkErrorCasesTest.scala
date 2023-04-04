package amf.poc

import amf.core.client.common.transform.PipelineId
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.IgnoringErrorHandler
import amf.core.internal.remote.{AmfJsonHint, Oas30JsonHint, Raml10YamlHint}
import amf.resolution.ResolutionTest

class ShapeNormalizationLinkErrorCasesTest extends ResolutionTest {

  val basePath = "amf-cli/shared/src/test/resources/poc-link-cases/"

  override def renderOptions(): RenderOptions = RenderOptions().withPrettyPrint.withSourceMaps.withFlattenedJsonLd

  ignore("Unresolved links are converted to RecursiveShape - Case 1") {
    cycle(
      "case1/api.json",
      "case1/api.resolved.jsonld",
      Oas30JsonHint,
      AmfJsonHint,
      pipeline = Option(PipelineId.Editing),
      eh = Some(IgnoringErrorHandler)
    )
  }

  ignore("Unresolved links are converted to RecursiveShape - Case 2") {
    cycle(
      "case2/api.json",
      "case2/api.resolved.jsonld",
      Oas30JsonHint,
      AmfJsonHint,
      pipeline = Option(PipelineId.Editing),
      eh = Some(IgnoringErrorHandler)
    )
  }

  ignore("Unresolved links are converted to RecursiveShape - Case 3") {
    cycle(
      "case3/api.json",
      "case3/api.resolved.jsonld",
      Oas30JsonHint,
      AmfJsonHint,
      pipeline = Option(PipelineId.Editing),
      eh = Some(IgnoringErrorHandler)
    )
  }
}
