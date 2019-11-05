package amf.plugins.document.webapi.parser.spec.oas

import amf.plugins.document.webapi.parser.spec.SpecSyntax

object Oas3Syntax extends SpecSyntax {
  override val nodes: Map[String, Set[String]] = Map(
    "paths" -> Set(),
    "webApi" -> Set(
      "openapi",
      "info",
      "servers",
      "paths",
      "components",
      "security",
      "tags",
      "externalDocs"
    ),
    "info" -> Set(
      "title",
      "description",
      "termsOfService",
      "contact",
      "license",
      "version"
    ),
    "contact" -> Set(
      "name",
      "url",
      "email"
    ),
    "license" -> Set(
      "name",
      "url"
    ),
    "xmlSerialization" -> Set(
      "attribute",
      "wrapped",
      "name",
      "namespace",
      "prefix"
    ),
    "pathItem" -> Set(
      "get",
      "put",
      "post",
      "delete",
      "options",
      "head",
      "patch",
      "connect",
      "trace",
      "parameters",
      "servers",
      "summary",
      "description",
      "\\$ref"
    ),
    "operation" -> Set(
      "tags",
      "summary",
      "description",
      "externalDocs",
      "operationId",
      "parameters",
      "requestBody",
      "responses",
      "callbacks",
      "deprecated",
      "security",
      "servers"
    ),
    "link" -> Set(
      "operationRef",
      "operationId",
      "description",
      "server",
      "parameters",
      "requestBody"
    ),
    "externalDoc" -> Set(
      "url"
    ),
    "parameter" -> Set(
      "name",
      "in",
      "description",
      "required",
      "deprecated",
      "allowEmptyValue",
      "style",
      "explode",
      "allowReserved",
      "schema",
      "example",
      "examples",
      "content"
    ),
    "header" -> Set(
      "description",
      "required",
      "schema",
      "format",
      "allowEmptyValue",
      "items",
      "collectionFormat",
      "default",
      "maximum",
      "exclusiveMaximum",
      "minimum",
      "exclusiveMinimum",
      "maxLength",
      "minLength",
      "pattern",
      "maxItems",
      "minItems",
      "multipleOf",
      "uniqueItems",
      "enum",
      "multipleOf",
      "items",
      "example"
    ),
    "request" -> Set(
      "description",
      "content",
      "required"
    ),
    "bodyParameter" -> Set(
      "name",
      "in",
      "description",
      "required",
      "schema"
    ),
    "response" -> Set(
      "description",
      "content",
      "headers",
      "links"
    ),
    "content" -> Set(
      "schema",
      "example",
      "examples",
      "encoding"
    ),
    "encoding" -> Set(
      "contentType",
      "headers",
      "style",
      "explode",
      "allowReserved"
    ),
    "headerParameter" -> Set(
      "description",
      "type",
      "items",
      "collectionFormat",
      "default",
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
      "enum",
      "multipleOf"
    ),
    "tag" -> Set(
      "name",
      "description",
      "externalDocs"
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

object Oas2Syntax extends SpecSyntax {

  override val nodes: Map[String, Set[String]] = Map(
    "paths" -> Set(), // paths in oas ignores the ones starting with '/', 'x-' and '$ref' but everything else is invalid
    "webApi" -> Set(
      "swagger",
      "info",
      "host",
      "basePath",
      "schemes",
      "consumes",
      "produces",
      "paths",
      "definitions",
      "parameters",
      "responses",
      "securityDefinitions",
      "security",
      "tags",
      "externalDocs"
    ),
    "info" -> Set(
      "title",
      "description",
      "termsOfService",
      "contact",
      "license",
      "version"
    ),
    "contact" -> Set(
      "name",
      "url",
      "email"
    ),
    "license" -> Set(
      "name",
      "url"
    ),
    "xmlSerialization" -> Set(
      "attribute",
      "wrapped",
      "name",
      "namespace",
      "prefix"
    ),
    "pathItem" -> Set(
      "get",
      "put",
      "post",
      "delete",
      "options",
      "head",
      "patch",
      "connect",
      "trace",
      "parameters",
      "$ref"
    ),
    "operation" -> Set(
      "tags",
      "summary",
      "description",
      "externalDocs",
      "operationId",
      "consumes",
      "produces",
      "parameters",
      "responses",
      "schemes",
      "deprecated",
      "security"
    ),
    "externalDoc" -> Set(
      "url"
    ),
    "parameter" -> Set(
      "name",
      "in",
      "description",
      "required",
      "type",
      "format",
      "allowEmptyValue",
      "items",
      "collectionFormat",
      "default",
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
      "enum",
      "multipleOf",
      "deprecated",
      "example"
    ),
    "bodyParameter" -> Set(
      "name",
      "in",
      "description",
      "required",
      "schema"
    ),
    "response" -> Set(
      "description",
      "schema",
      "headers",
      "examples"
    ),
    "headerParameter" -> Set(
      "description",
      "type",
      "items",
      "collectionFormat",
      "default",
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
      "enum",
      "multipleOf"
    ),
    "tag" -> Set(
      "name",
      "description",
      "externalDocs"
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
      "collectionFormat",
      "allOf",
      "properties",
      "additionalProperties",
      "discriminator",
      "readOnly",
      "xml",
      "externalDocs",
      "allOf",
      "anyOf",
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
