package amf.apicontract.internal.transformation.compatibility

import amf.apicontract.internal.spec.common.transformation.stage.{
  AnnotationRemovalStage,
  MediaTypeResolutionStage,
  RamlCompatiblePayloadAndParameterResolutionStage
}
import amf.apicontract.internal.transformation.compatibility.raml._
import amf.core.client.common.transform._
import amf.core.client.common.validation.{ProfileName, Raml08Profile, Raml10Profile}
import amf.core.client.scala.transform.{TransformationPipeline, TransformationStep}
import amf.core.internal.remote.{Raml08, Raml10}

class RamlCompatibilityPipeline private[amf] (override val name: String, profile: ProfileName)
    extends TransformationPipeline() {

  override def steps: Seq[TransformationStep] =
    Seq(
      new MandatoryDocumentationTitle(),
      new MandatoryAnnotationType(),
      new MediaTypeResolutionStage(profile, keepEditingInfo = true),
      new DefaultPayloadMediaType(),
      new MandatoryCreativeWorkFields(),
      new DefaultToNumericDefaultResponse(),
      new MakeExamplesOptional(),
      new CapitalizeSchemes(),
      new SecuritySettingsMapper(),
      new ShapeFormatAdjuster(),
      new CustomAnnotationDeclaration(),
      new PushSingleOperationPathParams(),
      new UnionsAsTypeExpressions(),
      new EscapeTypeNames(),
      new MakeRequiredFieldImplicitForOptionalProperties(),
      ResolveRamlCompatibleDeclarationsStage,
      new ResolveLinksWithNonDeclaredTargets(),
      new RamlCompatiblePayloadAndParameterResolutionStage(profile),
      new SanitizeCustomTypeNames(),
      new RecursionDetection(),
      new AnnotationRemovalStage()
    )
}

object Raml10CompatibilityPipeline {
  def apply()      = new RamlCompatibilityPipeline(name, Raml10Profile)
  val name: String = PipelineName.from(Raml10.mediaType, PipelineId.Compatibility)
}

object Raml08CompatibilityPipeline {
  def apply()      = new RamlCompatibilityPipeline(name, Raml08Profile)
  val name: String = PipelineName.from(Raml08.mediaType, PipelineId.Compatibility)
}
