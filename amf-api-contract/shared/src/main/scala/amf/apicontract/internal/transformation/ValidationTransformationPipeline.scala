package amf.apicontract.internal.transformation

import amf.aml.internal.transform.steps.SemanticExtensionFlatteningStage
import amf.apicontract.internal.spec.common.transformation.stage.{
  AmfParametersNormalizationStage,
  AnnotationRemovalStage,
  ChannelServersResolutionStage,
  MediaTypeResolutionStage,
  OpenApiParametersNormalizationStage,
  OperationsSecurityResolutionStage,
  ParametersNormalizationStage,
  PayloadAndParameterResolutionStage,
  Raml10ParametersNormalizationStage,
  ResponseExamplesResolutionStage
}
import amf.apicontract.internal.transformation.stages.ExtensionsResolutionStage
import amf.core.client.common.validation.{
  Async20Profile,
  GraphQLFederationProfile,
  GraphQLProfile,
  Oas20Profile,
  Oas30Profile,
  Oas31Profile,
  ProfileName,
  Raml08Profile,
  Raml10Profile
}
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.transform.{TransformationPipeline, TransformationPipelineRunner, TransformationStep}
import amf.core.internal.transform.stages.{ExternalSourceRemovalStage, ReferenceResolutionStage, SourceInformationStage}
import amf.core.internal.validation.ValidationConfiguration
import amf.shapes.internal.domain.resolution.ShapeNormalizationForUnitStage

// Validation pipeline is not registered in AMF configuration, is it only called internally.
class ValidationTransformationPipeline private[amf] (
    profile: ProfileName,
    override val name: String = "ValidationTransformationPipeline"
) extends TransformationPipeline() {

  override def steps: Seq[TransformationStep] =
    Seq(
      new ReferenceResolutionStage(keepEditingInfo = false),
      new ExternalSourceRemovalStage,
      new ExtensionsResolutionStage(profile, keepEditingInfo = false),
      new ShapeNormalizationForUnitStage(profile, keepEditingInfo = false),
      parameterNormalizationStageFor(profile),
      new MediaTypeResolutionStage(profile, isValidation = true),
      new ResponseExamplesResolutionStage(),
      new PayloadAndParameterResolutionStage(profile),
      new SemanticExtensionFlatteningStage,
      SourceInformationStage,
      new AnnotationRemovalStage(),
      new ChannelServersResolutionStage(profile),
      new OperationsSecurityResolutionStage(profile)
    )

  private def parameterNormalizationStageFor(profile: ProfileName): ParametersNormalizationStage = {
    profile match {
      case Raml10Profile => new Raml10ParametersNormalizationStage
      case Oas30Profile | Oas31Profile | Oas20Profile | Raml08Profile | Async20Profile =>
        new OpenApiParametersNormalizationStage
      case _ => new AmfParametersNormalizationStage
    }
  }
}

object ValidationTransformationPipeline {
  def apply(profile: ProfileName, unit: BaseUnit, configuration: ValidationConfiguration): BaseUnit = {
    val pipeline = profile match {
      // TODO: add RAML and other profiles
      case Oas30Profile                              => Oas3CachePipeline()
      case Oas31Profile                              => Oas31CachePipeline()
      case Async20Profile                            => Async20CachePipeline()
      case GraphQLProfile | GraphQLFederationProfile => GraphQLCachePipeline()
      case _                                         => new ValidationTransformationPipeline(profile)
    }
    val runner = TransformationPipelineRunner(configuration.eh, configuration.amfConfig)
    runner.run(unit, pipeline)
  }
}
