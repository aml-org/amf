package amf.shapes.internal.spec.async.parser

import amf.shapes.internal.spec.common.parser.SpecSyntax

object Async20ShapeSyntax extends SpecSyntax {
  override val nodes: Map[String, Set[String]] = Map(
    // Async Schema Object
    "schema" -> Set(
      "$ref",
      "$schema",
      "$comment",
      "$id",
      "format",
      "title",
      "description",
      "maximum",
      "exclusiveMaximum",
      "minimum",
      "exclusiveMinimum",
      "maxLength",
      "minLength",
      "pattern",
      "maxItems",
      "minItems",
      "uniqueItems",
      "maxProperties",
      "minProperties",
      "required",
      "enum",
      "type",
      "items",
      "additionalItems",
      "collectionFormat",
      "allOf",
      "properties",
      "additionalProperties",
      "propertyNames",
      "discriminator",
      "readOnly",
      "writeOnly",
      "deprecated",
      "externalDocs",
      "allOf",
      "anyOf",
      "oneOf",
      "not",
      "dependencies",
      "multipleOf",
      "default",
      "examples",
      "if",
      "then",
      "else",
      "const",
      "contains",
      "name",
      "patternProperties"
    )
  )
}
