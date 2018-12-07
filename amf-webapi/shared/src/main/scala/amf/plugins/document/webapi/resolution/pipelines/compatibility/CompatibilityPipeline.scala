package amf.plugins.document.webapi.resolution.pipelines.compatibility

import amf.core.parser.{ErrorHandler, UnhandledErrorHandler}
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.document.webapi.resolution.pipelines.Raml10ResolutionPipeline
import amf.{ProfileName, RamlProfile}

class CompatibilityPipeline(override val eh: ErrorHandler) extends ResolutionPipeline(eh) {

  private val resolution = new Raml10ResolutionPipeline(eh)

  override val steps: Seq[ResolutionStage] = resolution.steps ++ Seq(
    new MandatoryDocumentationTitle(),
    new SanitizeCustomTypeNames(),
    new MandatoryAnnotationType(),
    new DefaultPayloadMediaType(),
    new DefaultToNumericDefaultResponse(),
    new MakeExamplesOptional()
  )

  override def profileName: ProfileName = RamlProfile
}

object CompatibilityPipeline {
  def unhandled = new CompatibilityPipeline(UnhandledErrorHandler)
}
