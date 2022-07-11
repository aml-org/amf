package amf.shapes.internal.spec.oas.parser

import amf.shapes.internal.spec.common.parser.SpecSyntax

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
    "schema" -> Set(
      "$ref",
      "$schema",
      "format",
      "title",
      "description",
      "maximum",
      "exclusiveMaximum",
      "minimum",
      "exclusiveMinimum",
      "maxLength",
      "minLength",
      "nullable",
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
      "discriminator",
      "readOnly",
      "writeOnly",
      "xml",
      "deprecated",
      "externalDocs",
      "allOf",
      "anyOf",
      "oneOf",
      "not",
      "dependencies",
      "multipleOf",
      "default",
      "example",
      "id",
      "name",
      "patternProperties"
    )
  )
}
