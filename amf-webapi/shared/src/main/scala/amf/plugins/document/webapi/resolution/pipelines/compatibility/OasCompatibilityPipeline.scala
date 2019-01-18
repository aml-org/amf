package amf.plugins.document.webapi.resolution.pipelines.compatibility

import amf.{OasProfile, ProfileName}
import amf.core.parser.{ErrorHandler, UnhandledErrorHandler}
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.document.webapi.resolution.pipelines.OasResolutionPipeline
import amf.plugins.document.webapi.resolution.pipelines.compatibility.oas._

class OasCompatibilityPipeline(override val eh: ErrorHandler) extends ResolutionPipeline(eh) {

  private val resolution = new OasResolutionPipeline(eh)

  override val steps: Seq[ResolutionStage] = resolution.steps ++ Seq(
    new LowercaseSchemes(),
    new SecuritySettingsMapper(),
    new MandatoryDocumentationUrl(),
    new MandatoryResponses(),
    new MandatoryPathParameters(),
    new CleanNullSecurity()
  )

  override def profileName: ProfileName = OasProfile
}

object OasCompatibilityPipeline {
  def unhandled = new OasCompatibilityPipeline(UnhandledErrorHandler)
}
