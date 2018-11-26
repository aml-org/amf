package amf.plugins.document.webapi.resolution.pipelines

import amf.core.parser.ErrorHandler
import amf.{ProfileName, RamlProfile}

class Raml10ResolutionPipeline(override val eh: ErrorHandler) extends AmfResolutionPipeline(eh) {
  override def profileName: ProfileName = RamlProfile
}
