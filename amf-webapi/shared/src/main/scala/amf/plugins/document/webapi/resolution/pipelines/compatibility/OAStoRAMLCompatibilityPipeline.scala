package amf.plugins.document.webapi.resolution.pipelines.compatibility

import amf.core.parser.{ErrorHandler, UnhandledErrorHandler}
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.document.webapi.resolution.pipelines.Raml10ResolutionPipeline
import amf.plugins.document.webapi.resolution.pipelines.compatibility.raml._
import amf.{ProfileName, RamlProfile}

class OAStoRAMLCompatibilityPipeline(override val eh: ErrorHandler) extends ResolutionPipeline(eh) {

  private val resolution = new Raml10ResolutionPipeline(eh)

  override val steps: Seq[ResolutionStage] = resolution.steps ++ Seq(
    new MandatoryDocumentationTitle(),
    new SanitizeCustomTypeNames(),
    new MandatoryAnnotationType(),
    new DefaultPayloadMediaType(),
    new DefaultToNumericDefaultResponse(),
    new MakeExamplesOptional(),
    new CapitalizeSchemes(),
    new SecuritySettingsMapper(),
    new ShapeFormatAdjuster(),
    new CustomAnnotationDeclaration()
  )

  override def profileName: ProfileName = RamlProfile
}

object OAStoRAMLCompatibilityPipeline {
  def unhandled = new OAStoRAMLCompatibilityPipeline(UnhandledErrorHandler)
}
