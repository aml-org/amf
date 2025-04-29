package amf.shapes.internal.spec.jsonschema.semanticjsonschema.context

import amf.shapes.internal.spec.common.parser.SpecSyntax

object JsonSchemaSyntax extends SpecSyntax {
  override val nodes: Map[String, Set[String]] = Map(
    "schema" -> JsonSchemaFields
  )
}
