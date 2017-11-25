package amf.core.metamodel.document

import amf.core.metamodel.Field
import amf.core.metamodel.Type.Iri
import amf.core.vocabulary.Namespace.Document

object ExtensionLikeModel extends ExtensionLikeModel

trait ExtensionLikeModel extends DocumentModel {
  val Extends = Field(Iri, Document + "extends")

  override def fields: List[Field] = Extends :: DocumentModel.fields

}