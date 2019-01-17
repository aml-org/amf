package amf.plugins.document.webapi.resolution.pipelines.compatibility

import amf.{OasProfile, ProfileName}
import amf.core.parser.{ErrorHandler, UnhandledErrorHandler}
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.document.webapi.resolution.pipelines.OasResolutionPipeline
import amf.plugins.document.webapi.resolution.pipelines.compatibility.oas.{LowercaseSchemes, MandatoryDocumentationUrl, MandatoryResponses, SecuritySettingsMapper}

class OAStoRAMLCompatibiltyPipeline(override val eh: ErrorHandler) extends ResolutionPipeline(eh) {

  private val resolution = new OasResolutionPipeline(eh)

  override val steps: Seq[ResolutionStage] = resolution.steps ++ Seq(
    new LowercaseSchemes(),
    new SecuritySettingsMapper(),
    new MandatoryDocumentationUrl(),
    new MandatoryResponses()
  )

  override def profileName: ProfileName = OasProfile
}

object OAStoRAMLCompatibiltyPipeline {
  def unhandled = new OAStoRAMLCompatibiltyPipeline(UnhandledErrorHandler)
}

