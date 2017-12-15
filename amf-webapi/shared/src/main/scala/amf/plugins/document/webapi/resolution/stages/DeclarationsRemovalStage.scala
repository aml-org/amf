package amf.plugins.document.webapi.resolution.stages

import amf.core.metamodel.document.DocumentModel
import amf.core.model.document.{BaseUnit, Document}
import amf.core.resolution.stages.ResolutionStage

class DeclarationsRemovalStage(profile: String) extends ResolutionStage(profile) {
  override def resolve(model: BaseUnit): BaseUnit = {
    model match {
      case doc: Document  => doc.fields.remove(DocumentModel.Declares)
      case _              => // ignore
    }
    model
  }
}
