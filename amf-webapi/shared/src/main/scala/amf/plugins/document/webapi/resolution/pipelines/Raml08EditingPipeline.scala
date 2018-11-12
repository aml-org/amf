package amf.plugins.document.webapi.resolution.pipelines
import amf.core.model.document.BaseUnit
import amf.core.parser.ErrorHandler
import amf.{ProfileName, Raml08Profile}

class Raml08EditingPipeline(override val errorHandler: ErrorHandler) extends AmfEditingPipeline(errorHandler) {
  override def profileName: ProfileName = Raml08Profile
}
