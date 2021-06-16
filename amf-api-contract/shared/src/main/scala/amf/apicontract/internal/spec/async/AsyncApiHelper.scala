package amf.apicontract.internal.spec.async

import amf.apicontract.internal.metamodel.domain.OperationModel
import amf.core.internal.metamodel.Field

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
