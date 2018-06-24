package amf.plugins.document.webapi.resolution.pipelines

import amf.ProfileNames
import amf.ProfileNames.ProfileName
import amf.core.model.document.BaseUnit

class Raml08ResolutionPipeline(override val model: BaseUnit) extends AmfResolutionPipeline(model) {
  override def profileName: ProfileName = ProfileNames.RAML08
}
