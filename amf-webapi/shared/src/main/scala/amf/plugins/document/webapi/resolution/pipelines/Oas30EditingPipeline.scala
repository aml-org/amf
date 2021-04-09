package amf.plugins.document.webapi.resolution.pipelines
import amf.core.errorhandling.ErrorHandler
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.domain.shapes.resolution.stages.RequestParamsLinkStage
import amf.plugins.domain.webapi.resolution.stages.{OpenApiParametersNormalizationStage, ParametersNormalizationStage}
import amf.{Oas30Profile, ProfileName}

class Oas30EditingPipeline(urlShortening: Boolean = true)
    extends AmfEditingPipeline(urlShortening) {
  override def profileName: ProfileName = Oas30Profile
  override def references(implicit eh: ErrorHandler)               = new WebApiReferenceResolutionStage(true)

  override def parameterNormalizationStage(implicit eh: ErrorHandler): ParametersNormalizationStage = new OpenApiParametersNormalizationStage()

  override def steps(implicit eh: ErrorHandler): Seq[ResolutionStage] = Seq(
    new RequestParamsLinkStage(),
  ) ++ super.steps
}
