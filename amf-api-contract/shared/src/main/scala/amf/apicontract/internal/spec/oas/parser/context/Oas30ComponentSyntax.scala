package amf.apicontract.internal.spec.oas.parser.context

import amf.shapes.internal.spec.common.parser.SpecSyntax

object Oas30ComponentSyntax extends SpecSyntax {
  override val nodes: Map[String, Set[String]] = Oas3Syntax.nodes ++ Map(
    "info" -> Set(
      "title",
      "version"
    ),
    "root" -> Set(
      "openapi",
      "paths",
      "info",
      "components"
    )
  )
}
