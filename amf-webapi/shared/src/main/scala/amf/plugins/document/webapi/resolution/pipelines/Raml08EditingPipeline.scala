package amf.plugins.document.webapi.resolution.pipelines
import amf.core.parser.ErrorHandler
import amf.{ProfileName, Raml08Profile}

class Raml08EditingPipeline(override val eh: ErrorHandler) extends AmfEditingPipeline(eh) {
  override def profileName: ProfileName = Raml08Profile
  override def references               = new WebApiReferenceResolutionStage(true)
}
