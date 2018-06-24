package amf.core.resolution.stages

import amf.core.metamodel.document.DocumentModel
import amf.core.model.document.{BaseUnit, DeclaresModel, EncodesModel}
import amf.core.parser.ErrorHandler

class DeclarationsRemovalStage()(override implicit val errorHandler: ErrorHandler) extends ResolutionStage() {
  override def resolve[T <: BaseUnit](model: T): T = {
    model match {
      case doc: DeclaresModel with EncodesModel => doc.fields.removeField(DocumentModel.Declares)
      case _                                    => // ignore
    }
    model.asInstanceOf[T]
  }
}
