package amf.plugins.document.webapi.resolution.pipelines
import amf.core.model.document.BaseUnit
import amf.{ProfileName, Raml08Profile}

class Raml08EditingPipeline(override val model: BaseUnit) extends AmfEditingPipeline(model) {
  override def profileName: ProfileName = Raml08Profile
}
