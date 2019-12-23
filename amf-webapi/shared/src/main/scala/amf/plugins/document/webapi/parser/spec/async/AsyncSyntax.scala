package amf.plugins.document.webapi.parser.spec.async

import amf.plugins.document.webapi.parser.spec.SpecSyntax

// TODO ASYNC update
object Async20Syntax extends SpecSyntax {
  override val nodes: Map[String, Set[String]] = Map(
    "serverVariable" -> Set(
      "default",
      "description",
      "enum",
      "examples"
    ),
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
    ),
    "message" -> Set(
      "headers",
      "payload",
      "correlationId",
      "schemaFormat",
      "contentType",
      "name",
      "title",
      "summary",
      "description",
      "tags",
      "externalDocs",
      "bindings",
      "examples",
      "traits"
    ),
    "correlationId" -> Set(
      "description",
      "location"
    )
  )
}
