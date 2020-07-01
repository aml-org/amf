package amf.plugins.document.webapi.resolution.pipelines.compatibility

import amf.core.errorhandling.{ErrorHandler, UnhandledErrorHandler}
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.document.webapi.resolution.pipelines.compatibility.raml._
import amf.{ProfileName, RamlProfile}

class RamlCompatibilityPipeline(override val eh: ErrorHandler) extends ResolutionPipeline(eh) {

  override val steps: Seq[ResolutionStage] = Seq(
    new MandatoryDocumentationTitle(),
    new MandatoryAnnotationType(),
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
    new ResolveResponseAndParameterDeclarations(),
    new ResolveLinksWithNonDeclaredTargets(),
    new SanitizeCustomTypeNames(),
    new RecursionDetection()
  )

  override def profileName: ProfileName = RamlProfile
}

object RamlCompatibilityPipeline {
  def unhandled = new RamlCompatibilityPipeline(UnhandledErrorHandler)
}
