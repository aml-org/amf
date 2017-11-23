package amf.plugins.document.webapi.metamodel.model

import amf.framework.metamodel.document.DocumentModel
import amf.vocabulary.Namespace.Document
import amf.vocabulary.ValueType

object ExtensionModel extends ExtensionLikeModel {
  override val `type`: List[ValueType] = List(Document + "Extension") ++ DocumentModel.`type`
}
