package amf.plugins.document.webapi.resolution.pipelines
import amf.core.errorhandling.ErrorHandler
import amf.{OasProfile, ProfileName}

class OasResolutionPipeline(override val eh: ErrorHandler) extends AmfResolutionPipeline(eh) {
  override def profileName: ProfileName = OasProfile
  override def references               = new WebApiReferenceResolutionStage()
}
