package amf.core.resolution.stages

import amf.core.metamodel.document.DocumentModel
import amf.core.model.document.{BaseUnit, DeclaresModel, EncodesModel}
import amf.core.model.domain.AmfArray
import amf.core.parser.ErrorHandler

class DeclarationsRemovalStage()(override implicit val errorHandler: ErrorHandler) extends ResolutionStage() {

  override def resolve[T <: BaseUnit](model: T): T = {
    model match {
      case doc: DeclaresModel with EncodesModel => removeAllDeclarationsButSecuritySchemes(doc)
      case _                                    => // ignore
    }
    model.asInstanceOf[T]
  }

  private def removeAllDeclarationsButSecuritySchemes(doc: DeclaresModel) = {
    val schemes = doc.declares.filter(_.meta.`type`.head.iri() == "http://a.ml/vocabularies/security#SecurityScheme")

    if (schemes.isEmpty) {
      doc.fields.removeField(DocumentModel.Declares)
    } else {
      doc.fields.?[AmfArray](DocumentModel.Declares) match {
        case Some(array) => array.values = schemes
        case _           =>
      }
    }
  }
}
