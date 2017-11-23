package amf.plugins.document.webapi.metamodel.model

import amf.framework.metamodel.Field
import amf.framework.metamodel.document.DocumentModel
import amf.vocabulary.Namespace.Document
import amf.framework.metamodel.Type.Iri

object ExtensionLikeModel extends ExtensionLikeModel

trait ExtensionLikeModel extends DocumentModel {
  val Extends = Field(Iri, Document + "extends")

  override def fields: List[Field] = Extends :: DocumentModel.fields

}