package amf.plugins.document.webapi.resolution.pipelines

import amf.core.model.document.BaseUnit
import amf.{ProfileName, Raml08Profile}

class Raml08ResolutionPipeline(override val model: BaseUnit) extends AmfResolutionPipeline(model) {
  override def profileName: ProfileName = Raml08Profile
}
