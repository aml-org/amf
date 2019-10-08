package amf.plugins.document.webapi.resolution.pipelines
import amf.core.parser.ErrorHandler
import amf.{Oas30Profile, ProfileName}

class Oas30EditingPipeline(override val eh: ErrorHandler) extends AmfEditingPipeline(eh) {
  override def profileName: ProfileName = Oas30Profile
  override def references               = new WebApiReferenceResolutionStage(true)
}
