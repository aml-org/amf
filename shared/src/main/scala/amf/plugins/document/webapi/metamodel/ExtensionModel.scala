package amf.plugins.document.webapi.metamodel

import amf.framework.metamodel.document.{DocumentModel, ExtensionLikeModel}
import amf.plugins.document.webapi.model.Extension
import amf.vocabulary.Namespace.Document
import amf.vocabulary.ValueType

object ExtensionModel extends ExtensionLikeModel {
  override val `type`: List[ValueType] = List(Document + "Extension") ++ DocumentModel.`type`
  override def modelInstance = Extension()
}
