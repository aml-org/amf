package amf.apicontract.internal.spec.async.parser.context.syntax

import amf.shapes.internal.spec.common.parser.SyntaxHelper._
import amf.apicontract.internal.spec.async.parser.bindings.Bindings.AnypointMQ
import amf.shapes.internal.spec.common.parser.SpecSyntax

object Async22Syntax extends SpecSyntax {
  override val nodes: Map[String, Set[String]] =
    add(
      Async21Syntax.nodes,
      "bindings" -> Set(AnypointMQ),
      "pathItem" -> Set(
        "servers"
      ),
      "AnypointMQMessageBinding" -> Set(
        "headers",
        "bindingVersion"
      ),
      "AnypointMQChannelBinding" -> Set(
        "destination",
        "destinationType",
        "bindingVersion"
      )
    )
}
