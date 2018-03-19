package amf.core.resolution.stages

import amf.core.metamodel.document.DocumentModel
import amf.core.model.document.{BaseUnit, DeclaresModel, EncodesModel}

class DeclarationsRemovalStage(profile: String) extends ResolutionStage(profile) {
  override def resolve(model: BaseUnit): BaseUnit = {
    model match {
      case doc: DeclaresModel with EncodesModel  => doc.fields.remove(DocumentModel.Declares)
      case _                                     => // ignore
    }
    model
  }
}
