package amf.apicontract.internal.spec.async.parser.context.syntax

import amf.shapes.internal.spec.common.parser.SpecSyntax

object Async25Syntax extends SpecSyntax {
  override val nodes: Map[String, Set[String]] = add(Async24Syntax.nodes,
    "server" -> Set(
      "tags"
    )
  )
}
