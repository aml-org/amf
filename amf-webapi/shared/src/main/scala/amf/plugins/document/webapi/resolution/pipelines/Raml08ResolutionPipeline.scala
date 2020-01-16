package amf.plugins.document.webapi.resolution.pipelines

import amf.core.errorhandling.ErrorHandler
import amf.{ProfileName, Raml08Profile}

class Raml08ResolutionPipeline(override val eh: ErrorHandler) extends AmfResolutionPipeline(eh) {
  override def profileName: ProfileName = Raml08Profile
  override def references               = new WebApiReferenceResolutionStage()
}
