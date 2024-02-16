package amf.apicontract.internal.spec.async.parser.context.syntax

import amf.shapes.internal.spec.common.parser.SpecSyntax

object Async22Syntax extends SpecSyntax {
  override val nodes: Map[String, Set[String]] =
    add(
      Async21Syntax.nodes,
      "pathItem" -> Set(
        "description",
        "servers",
        "subscribe",
        "publish",
        "parameters",
        "bindings"
      )
    )
}
