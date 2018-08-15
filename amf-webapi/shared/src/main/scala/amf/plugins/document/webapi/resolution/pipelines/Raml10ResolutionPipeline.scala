package amf.plugins.document.webapi.resolution.pipelines

import amf.core.model.document.BaseUnit
import amf.{ProfileName, RamlProfile}

class Raml10ResolutionPipeline(override val model: BaseUnit) extends AmfResolutionPipeline(model) {
  override def profileName: ProfileName = RamlProfile
}
