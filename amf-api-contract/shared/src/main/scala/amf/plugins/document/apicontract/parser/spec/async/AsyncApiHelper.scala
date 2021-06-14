package amf.plugins.document.apicontract.parser.spec.async

import amf.core.internal.metamodel.Field
import amf.plugins.domain.apicontract.metamodel.OperationModel

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
