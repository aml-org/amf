package amf.plugins.document.webapi.metamodel

import amf.framework.metamodel.document.{DocumentModel, ExtensionLikeModel}
import amf.plugins.document.webapi.model.Overlay
import amf.framework.vocabulary.Namespace.Document
import amf.framework.vocabulary.ValueType

object OverlayModel extends ExtensionLikeModel {
  override val `type`: List[ValueType] = List(Document + "Overlay") ++ DocumentModel.`type`

  override def modelInstance = Overlay()
}
