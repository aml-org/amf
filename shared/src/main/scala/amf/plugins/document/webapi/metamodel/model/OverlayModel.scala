package amf.plugins.document.webapi.metamodel.model

import amf.framework.metamodel.document.DocumentModel
import amf.vocabulary.Namespace.Document
import amf.vocabulary.ValueType

object OverlayModel extends ExtensionLikeModel {
  override val `type`: List[ValueType] = List(Document + "Overlay") ++ DocumentModel.`type`
}
