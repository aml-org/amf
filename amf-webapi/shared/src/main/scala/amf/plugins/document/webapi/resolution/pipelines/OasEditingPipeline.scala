package amf.plugins.document.webapi.resolution.pipelines
import amf.core.model.document.BaseUnit
import amf.{OasProfile, ProfileName}

class OasEditingPipeline(override val model: BaseUnit) extends AmfEditingPipeline(model) {
  override def profileName: ProfileName = OasProfile
  override val references               = new OasReferenceResolutionStage() // should be resolution pipeline true right?
}
