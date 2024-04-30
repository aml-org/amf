package amf.apicontract.internal.spec.async.parser.context.syntax

import amf.apicontract.internal.spec.async.parser.bindings.Bindings.Solace
import amf.shapes.internal.spec.common.parser.SpecSyntax

object Async23Syntax extends SpecSyntax {
  override val nodes: Map[String, Set[String]] =
    add(
      Async22Syntax.nodes,
      "bindings" -> Set(Solace),
      "components" -> Set(
        "servers",
        "channels"
      ),
      "SolaceServerBinding" -> Set(
        "msgVpn",
        "bindingVersion"
      ),
      "SolaceOperationBinding" -> Set(
        "destinations",
        "bindingVersion"
      ),
      "SolaceOperationDestination010" -> Set(
        "destinationType",
        "deliveryMode",
        "queue"
      ),
      "SolaceOperationDestination020" -> Set(
        "destinationType",
        "deliveryMode",
        "queue",
        "topic"
      ),
      "SolaceOperationQueue" -> Set(
        "name",
        "topicSubscriptions",
        "accessType",
        "maxMsgSpoolSize",
        "maxTtl"
      ),
      "SolaceOperationTopic" -> Set(
        "topicSubscriptions"
      )
    )
}
