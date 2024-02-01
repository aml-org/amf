package amf.apicontract.internal.spec.async.parser.context.syntax

import amf.apicontract.internal.spec.async.parser.bindings.Bindings.Mercure
import amf.shapes.internal.spec.common.parser.SpecSyntax

object Async21Syntax extends SpecSyntax {
  override val nodes: Map[String, Set[String]] = add(Async20Syntax.nodes, "bindings", Set(Mercure))
}
