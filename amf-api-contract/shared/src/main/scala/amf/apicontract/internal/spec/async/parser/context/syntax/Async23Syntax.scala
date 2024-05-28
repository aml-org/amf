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
      "SolaceServerBinding010" -> Set(
        "msgVpn",
        "bindingVersion"
      ),
      "SolaceServerBinding040" -> Set(
        "msgVpn",
        "bindingVersion",
        "clientName"
      ),
      "SolaceOperationBinding010" -> Set(
        "destinations",
        "bindingVersion"
      ),
      "SolaceOperationBinding040" -> Set(
        "destinations",
        "bindingVersion",
        "timeToLive",
        "priority",
        "dmqEligible"
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
      "SolaceOperationDestination040" -> Set(
        "destinationType",
        "deliveryMode",
        "bindingVersion",
        "queue",
        "topic"
      ),
      "SolaceOperationQueue010" -> Set(
        "name",
        "topicSubscriptions",
        "accessType",
      ),
      "SolaceOperationQueue030" -> Set(
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
