package amf.plugins.document.webapi.resolution.pipelines
import amf.core.errorhandling.ErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.domain.shapes.resolution.stages.RequestParamsLinkStage
import amf.plugins.domain.webapi.resolution.stages.{OpenApiParametersNormalizationStage, ParametersNormalizationStage}
import amf.{Oas30Profile, ProfileName}

class Oas30ResolutionPipeline() extends AmfResolutionPipeline() {
  override def profileName: ProfileName = Oas30Profile
  override def references(implicit eh: ErrorHandler)               = new WebApiReferenceResolutionStage()

  override protected def parameterNormalizationStage(implicit eh: ErrorHandler): ParametersNormalizationStage =
    new OpenApiParametersNormalizationStage()

  override def steps(model: BaseUnit, sourceVendor: String)(implicit errorHandler: ErrorHandler): Seq[ResolutionStage] = Seq(
    new RequestParamsLinkStage(),
  ) ++ super.steps(model, sourceVendor)
}
