package amf.apicontract.internal.spec.oas.parser.context

import amf.shapes.internal.spec.common.parser.{Oas2ShapeSyntax, Oas3ShapeSyntax, SpecSyntax}

object Oas3Syntax extends SpecSyntax {
  override val nodes: Map[String, Set[String]] = Oas3ShapeSyntax.nodes ++ Map(
    "paths" -> Set.empty[String],
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

  override val nodes: Map[String, Set[String]] = Oas2ShapeSyntax.nodes ++ Map(
    "paths" -> Set
      .empty[String], // paths in oas ignores the ones starting with '/', 'x-' and '$ref' but everything else is invalid
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
