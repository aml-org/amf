package amf.shapes.internal.document.metamodel

import amf.core.client.scala.vocabulary.Namespace.Document
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.document.DocumentModel

object JsonLDInstanceDocumentModel extends DocumentModel {

  override val `type`: List[ValueType] = (Document + "JsonLDInstanceDocument") :: DocumentModel.`type`
}
