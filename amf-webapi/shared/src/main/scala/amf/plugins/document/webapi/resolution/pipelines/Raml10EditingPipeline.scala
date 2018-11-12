package amf.plugins.document.webapi.resolution.pipelines

import amf.core.parser.ErrorHandler
import amf.{ProfileName, RamlProfile}

class Raml10EditingPipeline(override val eh: ErrorHandler) extends AmfEditingPipeline(eh) {
  override def profileName: ProfileName = RamlProfile
}
