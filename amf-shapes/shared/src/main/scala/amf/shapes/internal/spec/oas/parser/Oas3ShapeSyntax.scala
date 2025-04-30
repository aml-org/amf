package amf.shapes.internal.spec.oas.parser

import amf.shapes.internal.spec.common.parser.SyntaxHelper._
import amf.shapes.internal.spec.common.parser.SpecSyntax

object Oas31ShapeSyntax extends SpecSyntax {
  override val nodes: Map[String, Set[String]] =
    remove(
      Oas3ShapeSyntax.nodes,
      "schema" -> Set("nullable")
    )
}

object Oas3ShapeSyntax extends SpecSyntax {
  override val nodes: Map[String, Set[String]] = Map(
    "xmlSerialization" -> Set(
      "attribute",
      "wrapped",
      "name",
      "namespace",
      "prefix"
    ),
    "example" -> Set(
      "summary",
      "description",
      "value",
      "externalValue"
    ),
    "discriminator" -> Set(
      "propertyName",
      "mapping"
    ),
    "schema" -> JsonSchemaFields
  )
}
