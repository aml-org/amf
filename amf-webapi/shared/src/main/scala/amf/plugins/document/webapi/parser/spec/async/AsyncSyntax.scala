package amf.plugins.document.webapi.parser.spec.async

import amf.plugins.document.webapi.parser.spec.SpecSyntax

object Async20Syntax extends SpecSyntax {
  override val nodes: Map[String, Set[String]] = Map(
    "webApi" -> Set(
      "asyncapi",
      "id",
      "info",
      "servers",
      "channels",
      "components",
      "tags",
      "externalDocs"
    ),
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
    ),
    "parameter" -> Set(
      "description",
      "schema",
      "location"
    ),
    "pathItem" -> Set(
      "description",
      "subscribe",
      "publish",
      "parameters",
      "bindings"
    ),
    "operation" -> Set(
      "operationId",
      "summary",
      "description",
      "tags",
      "externalDocs",
      "bindings",
      "traits",
      "message"
    ),
    "info" -> Set(
      "title",
      "description",
      "termsOfService",
      "contact",
      "license",
      "version"
    )
  )
}
