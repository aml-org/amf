package amf.apicontract.internal.spec.oas.parser

import amf.shapes.internal.spec.common.parser.SpecSyntax

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
    "components" -> Set(
      "schemas",
      "responses",
      "parameters",
      "examples",
      "requestBodies",
      "headers",
      "securitySchemes",
      "links",
      "callbacks"
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
    "server" -> Set(
      "url",
      "description",
      "variables"
    ),
    "serverVariable" -> Set(
      "enum",
      "default",
      "description"
    ),
    "externalDoc" -> Set(
      "url",
      "description"
    ),
    "example" -> Set(
      "summary",
      "description",
      "value",
      "externalValue"
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
    ),
    "securityScheme" -> Set(
      "type",
      "description",
      "name",
      "in",
      "scheme",
      "bearerFormat",
      "flows",
      "openIdConnectUrl"
    ),
    "clientCredentials" -> Set(
      "refreshUrl",
      "tokenUrl",
      "scopes"
    ),
    "implicit" -> Set(
      "authorizationUrl",
      "refreshUrl",
      "scopes"
    ),
    "password" -> Set(
      "refreshUrl",
      "tokenUrl",
      "scopes"
    ),
    "authorizationCode" -> Set(
      "authorizationUrl",
      "refreshUrl",
      "tokenUrl",
      "scopes"
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
    "server" -> Set(
      "url",
      "description",
      "variables"
    ),
    "serverVariable" -> Set(
      "enum",
      "default",
      "description"
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
      "url",
      "description"
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
      "additionalItems",
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
      "not",
      "dependencies",
      "multipleOf",
      "default",
      "example",
      "id",
      "name",
      "patternProperties"
    ),
    "securityScheme" -> Set(
      "type",
      "description",
      "name",
      "in",
      "flow",
      "authorizationUrl",
      "tokenUrl",
      "scopes"
    )
  )
}
