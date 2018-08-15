package amf.plugins.document.webapi.resolution.pipelines

import amf.core.model.document.BaseUnit
import amf.{ProfileName, RamlProfile}

class Raml10EditingPipeline(override val model: BaseUnit) extends AmfEditingPipeline(model) {
  override def profileName: ProfileName = RamlProfile
}
