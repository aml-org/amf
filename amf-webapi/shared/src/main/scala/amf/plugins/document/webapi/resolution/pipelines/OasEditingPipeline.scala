package amf.plugins.document.webapi.resolution.pipelines
import amf.core.errorhandling.ErrorHandler
import amf.{OasProfile, ProfileName}

class OasEditingPipeline(override val eh: ErrorHandler, urlShortening: Boolean = true)
    extends AmfEditingPipeline(eh, urlShortening) {
  override def profileName: ProfileName = OasProfile
  override def references               = new WebApiReferenceResolutionStage(true)
}
