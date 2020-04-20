package amf.plugins.document.webapi.resolution.pipelines
import amf.core.errorhandling.ErrorHandler
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.domain.shapes.resolution.stages.TypeAliasTransformationStage
import amf.plugins.domain.webapi.resolution.stages.{OpenApiParametersNormalizationStage, ParametersNormalizationStage}
import amf.{ProfileName, Raml08Profile}

class Raml08EditingPipeline(override val eh: ErrorHandler, urlShortening: Boolean = true)
    extends AmfEditingPipeline(eh, urlShortening) {
  override def profileName: ProfileName = Raml08Profile
  override def references               = new WebApiReferenceResolutionStage(true)

  override def parameterNormalizationStage: ParametersNormalizationStage = new OpenApiParametersNormalizationStage()

  override val steps: Seq[ResolutionStage] = Seq(
    new TypeAliasTransformationStage(),
  ) ++ baseSteps
}
