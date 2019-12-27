package amf.plugins.document.webapi.resolution.pipelines

import amf.core.errorhandling.ErrorHandler
import amf.{ProfileName, RamlProfile}

class Raml10EditingPipeline(override val eh: ErrorHandler, urlShortening: Boolean = true)
    extends AmfEditingPipeline(eh, urlShortening) {
  override def profileName: ProfileName = RamlProfile
  override def references               = new WebApiReferenceResolutionStage(true)
}
