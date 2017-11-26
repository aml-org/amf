package amf.plugins.document.webapi.parser.spec.raml

import amf.plugins.document.webapi.parser.spec.SpecSyntax

object RamlSyntax extends SpecSyntax {

  override val nodes: Map[String, Set[String]] = Map(
    "webApi" -> Set(
      "title",
      "description",
      "version",
      "baseUri",
      "baseUriParameters",
      "protocols",
      "mediaType",
      "documentation",
      "schemas",
      "types",
      "traits",
      "resourceTypes",
      "annotationTypes",
      "securitySchemes",
      "securedBy",
      "usage",
      "extends",
      "uses"
    ),
    "userDocumentation" -> Set(
      "title",
      "content"
    ),
    "shape" -> Set(
      "type",
      "default",
      "schema",
      "example",
      "examples",
      "displayName",
      "description",
      "facets",
      "xml",
      "enum",
      "required"
    ),
    "unionShape" -> Set(
      "type",
      "default",
      "schema",
      "example",
      "examples",
      "displayName",
      "description",
      "facets",
      "xml",
      "enum",
      "required",
      "anyOf"
    ),
    "nodeShape" -> Set(
      "type",
      "default",
      "schema",
      "example",
      "examples",
      "displayName",
      "description",
      "facets",
      "xml",
      "enum",
      "properties",
      "minProperties",
      "maxProperties",
      "discriminator",
      "discriminatorValue",
      "required",
      "additionalProperties"
    ),
    "arrayShape" -> Set(
      "type",
      "default",
      "schema",
      "example",
      "examples",
      "displayName",
      "description",
      "facets",
      "xml",
      "enum",
      "uniqueItems",
      "items",
      "minItems",
      "maxItems",
      "required"
    ),
    "stringScalarShape" -> Set(
      "type",
      "default",
      "schema",
      "example",
      "examples",
      "displayName",
      "description",
      "facets",
      "xml",
      "enum",
      "pattern",
      "minLength",
      "maxLength",
      "required"
    ),
    "numberScalarShape" -> Set(
      "type",
      "default",
      "schema",
      "example",
      "examples",
      "displayName",
      "description",
      "facets",
      "xml",
      "enum",
      "minimum",
      "maximum",
      "format",
      "multipleOf",
      "required"
    ),
    "dateScalarShape" -> Set(
      "type",
      "default",
      "schema",
      "example",
      "examples",
      "displayName",
      "description",
      "facets",
      "xml",
      "enum",
      "required",
      "format"
    ),
    "fileShape" -> Set(
      "type",
      "default",
      "schema",
      "example",
      "examples",
      "displayName",
      "description",
      "facets",
      "xml",
      "enum",
      "fileTypes",
      "minLength",
      "maxLength",
      "required"
    ),
    "example" -> Set(
      "displayName",
      "description",
      "value",
      "strict"
    ),
    "xmlSerialization" -> Set(
      "attribute",
      "wrapped",
      "name",
      "namespace",
      "prefix"
    ),
    "endPoint" -> Set(
      "displayName",
      "description",
      "get",
      "patch",
      "put",
      "post",
      "delete",
      "options",
      "head",
      "get?",
      "patch?",
      "put?",
      "post?",
      "delete?",
      "options?",
      "head?",
      "is",
      "type",
      "securedBy",
      "uriParameters"
    ),
    "operation" -> Set(
      "displayName",
      "description",
      "queryParameters",
      "headers",
      "queryString",
      "responses",
      "body",
      "protocols",
      "is",
      "securedBy"
    ),
    "response" -> Set(
      "displayName",
      "description",
      "headers",
      "body"
    ),
    "securitySchema" -> Set(
      "type",
      "displayName",
      "description",
      "decribedBy",
      "settings"
    ),
    "annotation" -> Set(
      "displayName",
      "description",
      "allowedTargets"
    )
  )
}
