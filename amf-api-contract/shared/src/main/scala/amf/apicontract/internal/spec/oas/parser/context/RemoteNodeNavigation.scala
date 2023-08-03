package amf.apicontract.internal.spec.oas.parser.context

import org.yaml.model.YNode

case class RemoteNodeNavigation[T <: OasLikeWebApiContext](remoteNode: YNode, context: T)

object RemoteNodeNavigation {

  def unapply[T <: OasLikeWebApiContext](arg: RemoteNodeNavigation[T]): Option[(YNode, T)] =
    Some((arg.remoteNode, arg.context))
}
