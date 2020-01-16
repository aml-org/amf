package amf.plugins.document.webapi.resolution.pipelines
import amf.core.errorhandling.ErrorHandler
import amf.{Oas30Profile, ProfileName}

class Oas30EditingPipeline(override val eh: ErrorHandler, urlShortening: Boolean = true)
    extends AmfEditingPipeline(eh, urlShortening) {
  override def profileName: ProfileName = Oas30Profile
  override def references               = new WebApiReferenceResolutionStage(true)
}
