package amf.plugins.document.webapi.resolution.pipelines

import amf.core.errorhandling.ErrorHandler
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.domain.shapes.resolution.stages.ShapeNormalizationStage
import amf.plugins.domain.webapi.resolution.stages._
import amf.plugins.domain.webapi.resolution.stages.async.{
  AsyncContentTypeResolutionStage,
  AsyncExamplePropagationResolutionStage,
  ServerVariableExampleResolutionStage
}
import amf.{Async20Profile, ProfileName}

class Async20EditingPipeline(override val eh: ErrorHandler, urlShortening: Boolean = true)
    extends AmfEditingPipeline(eh, urlShortening) {
  override def profileName: ProfileName = Async20Profile

  override def references = new WebApiReferenceResolutionStage(true)

  override protected def parameterNormalizationStage: ParametersNormalizationStage =
    new OpenApiParametersNormalizationStage()

  override val steps: Seq[ResolutionStage] = Seq(
    references,
    new ShapeNormalizationStage(profileName, keepEditingInfo = true),
    new JsonMergePatchStage(),
    new AsyncContentTypeResolutionStage(),
    new AsyncExamplePropagationResolutionStage(),
    new ServerVariableExampleResolutionStage(),
    new PathDescriptionNormalizationStage(profileName, keepEditingInfo = true)
  )
}
