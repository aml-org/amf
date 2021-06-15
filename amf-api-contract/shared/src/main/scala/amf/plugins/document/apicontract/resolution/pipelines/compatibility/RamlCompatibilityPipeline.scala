package amf.plugins.document.apicontract.resolution.pipelines.compatibility

import amf.core.client.common.validation.{ProfileName, Raml08Profile, Raml10Profile}
import amf.core.client.scala.transform.PipelineName
import amf.core.client.scala.transform.pipelines.TransformationPipeline
import amf.core.client.scala.transform.stages.TransformationStep
import amf.core.internal.remote.{Raml08, Raml10}
import amf.plugins.document.apicontract.resolution.pipelines.compatibility.raml._
import amf.plugins.domain.apicontract.resolution.stages.{
  AnnotationRemovalStage,
  MediaTypeResolutionStage,
  RamlCompatiblePayloadAndParameterResolutionStage
}

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
  val name: String = PipelineName.from(Raml10.name, TransformationPipeline.COMPATIBILITY_PIPELINE)
}

object Raml08CompatibilityPipeline {
  def apply()      = new RamlCompatibilityPipeline(name, Raml08Profile)
  val name: String = PipelineName.from(Raml08.name, TransformationPipeline.COMPATIBILITY_PIPELINE)
}
