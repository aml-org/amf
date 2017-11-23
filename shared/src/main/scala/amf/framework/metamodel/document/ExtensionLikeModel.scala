package amf.framework.metamodel.document

import amf.framework.metamodel.Field
import amf.framework.metamodel.Type.Iri
import amf.vocabulary.Namespace.Document

object ExtensionLikeModel extends ExtensionLikeModel

trait ExtensionLikeModel extends DocumentModel {
  val Extends = Field(Iri, Document + "extends")

  override def fields: List[Field] = Extends :: DocumentModel.fields

}