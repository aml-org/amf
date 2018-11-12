package amf.plugins.document.webapi.resolution.pipelines
import amf.core.model.document.BaseUnit
import amf.core.parser.ErrorHandler
import amf.{OasProfile, ProfileName}

class OasEditingPipeline(override val eh: ErrorHandler) extends AmfEditingPipeline(eh) {
  override def profileName: ProfileName = OasProfile
  override val references               = new OasReferenceResolutionStage() // should be resolution pipeline true right?
}
