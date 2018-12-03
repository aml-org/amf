package amf.plugins.document.webapi.resolution.pipelines
import amf.core.parser.ErrorHandler
import amf.{OasProfile, ProfileName}

class OasEditingPipeline(override val eh: ErrorHandler) extends AmfEditingPipeline(eh) {
  override def profileName: ProfileName = OasProfile
  override def references               = new WebApiReferenceResolutionStage(true)
}
