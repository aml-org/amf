package amf.plugins.document.webapi.parser.spec.async

import amf.core.metamodel.Field
import amf.plugins.domain.webapi.metamodel.OperationModel

sealed trait MessageType {
  def field: Field
}
case object Publish extends MessageType {
  override def field: Field = OperationModel.Request
}
case object Subscribe extends MessageType {
  override def field: Field = OperationModel.Responses
}

object AsyncHelper {

  def messageType(messageType: String): Option[MessageType] =
    messageType match {
      case "publish"   => Some(Publish)
      case "subscribe" => Some(Subscribe)
      case _           => None
    }
}
