package amf.plugins.document.webapi.resolution.pipelines

import amf.ProfileNames
import amf.ProfileNames.ProfileName
import amf.core.model.document.BaseUnit

class OasEditingPipeline(override val model: BaseUnit) extends AmfEditingPipeline(model) {
  override def profileName: ProfileName = ProfileNames.OAS
  override val references               = new OasReferenceResolutionStage() // should be resolution pipeline true right?
}
