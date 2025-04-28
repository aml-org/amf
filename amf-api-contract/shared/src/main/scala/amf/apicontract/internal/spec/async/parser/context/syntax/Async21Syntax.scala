package amf.apicontract.internal.spec.async.parser.context.syntax

import amf.shapes.internal.spec.common.parser.SyntaxHelper._
import amf.apicontract.internal.spec.async.parser.bindings.Bindings.{IBMMQ, Mercure}
import amf.shapes.internal.spec.common.parser.SpecSyntax

object Async21Syntax extends SpecSyntax {
  override val nodes: Map[String, Set[String]] =
    add(
      Async20Syntax.nodes,
      "bindings" -> Set(Mercure, IBMMQ),
      "IBMMQMessageBinding" -> Set(
        "type",
        "headers",
        "description",
        "expiry",
        "bindingVersion"
      ),
      "IBMMQServerBinding" -> Set(
        "groupId",
        "ccdtQueueManagerName",
        "cipherSpec",
        "multiEndpointServer",
        "heartBeatInterval",
        "bindingVersion"
      ),
      "IBMMQChannelBinding" -> Set(
        "destinationType",
        "queue",
        "topic",
        "maxMsgLength",
        "bindingVersion"
      ),
      "IBMMQChannelQueue" -> Set(
        "objectName",
        "isPartitioned",
        "exclusive"
      ),
      "IBMMQChannelTopic" -> Set(
        "string",
        "objectName",
        "durablePermitted",
        "lastMsgRetained"
      ),
      "message examples" -> Set(
        "name",
        "summary"
      )
    )
}
