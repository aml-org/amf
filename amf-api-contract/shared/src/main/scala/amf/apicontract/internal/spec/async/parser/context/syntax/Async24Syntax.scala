package amf.apicontract.internal.spec.async.parser.context.syntax

import amf.shapes.internal.spec.common.parser.SyntaxHelper._
import amf.shapes.internal.spec.common.parser.SpecSyntax

object Async24Syntax extends SpecSyntax {
  override val nodes: Map[String, Set[String]] =
    add(
      Async23Syntax.nodes,
      "message" -> Set("messageId"),
      "components" -> Set(
        "serverVariables"
      ),
      "operation" -> Set(
        "security"
      )
    )
}
