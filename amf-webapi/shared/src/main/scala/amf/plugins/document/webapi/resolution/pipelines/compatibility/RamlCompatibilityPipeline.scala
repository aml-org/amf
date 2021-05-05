package amf.plugins.document.webapi.resolution.pipelines.compatibility

import amf.{ProfileName, Raml08Profile, Raml10Profile}
import amf.client.remod.amfcore.resolution.PipelineName
import amf.core.errorhandling.ErrorHandler
import amf.core.remote.{Raml08, Raml10}
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.document.webapi.resolution.pipelines.compatibility.raml._
import amf.plugins.domain.webapi.resolution.stages.{
  AnnotationRemovalStage,
  MediaTypeResolutionStage,
  RamlCompatiblePayloadAndParameterResolutionStage
}

class RamlCompatibilityPipeline private[amf] (override val name: String, profile: ProfileName)
    extends ResolutionPipeline() {

  override def steps: Seq[ResolutionStage] =
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
  val name: String = PipelineName.from(Raml10.name, ResolutionPipeline.COMPATIBILITY_PIPELINE)
}

object Raml08CompatibilityPipeline {
  def apply()      = new RamlCompatibilityPipeline(name, Raml08Profile)
  val name: String = PipelineName.from(Raml08.name, ResolutionPipeline.COMPATIBILITY_PIPELINE)
}
