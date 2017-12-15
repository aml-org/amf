package amf.core.resolution.stages

import amf.core.metamodel.document.BaseUnitModel
import amf.core.model.document.BaseUnit

class CleanReferencesStage(profile: String) extends ResolutionStage(profile) {
  override def resolve(model: BaseUnit): BaseUnit = {
    model.fields.remove(BaseUnitModel.References)
    model
  }
}
