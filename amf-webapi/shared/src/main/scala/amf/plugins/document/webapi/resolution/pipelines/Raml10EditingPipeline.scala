package amf.plugins.document.webapi.resolution.pipelines

import amf.ProfileNames
import amf.ProfileNames.ProfileName
import amf.core.model.document.BaseUnit

class Raml10EditingPipeline(override val model: BaseUnit) extends AmfEditingPipeline(model) {
  override def profileName: ProfileName = ProfileNames.RAML
}
