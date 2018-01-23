package amf.plugins.document.webapi.parser.spec.raml

import amf.plugins.document.webapi.parser.spec.SpecSyntax

object Raml10Syntax extends RamlSyntax {
  override val nodes: Map[String, Set[String]] = commonNodes ++ Map(
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
      "uriParameters",
      "usage"
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
      "securedBy",
      "usage"
    ),
    "annotation" -> Set(
      "displayName",
      "description",
      "allowedTargets"
    )
  )
}

object Raml08Syntax extends RamlSyntax {
  override val nodes: Map[String, Set[String]] = commonNodes ++ Map(
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
      "traits",
      "resourceTypes",
      "securitySchemes",
      "securedBy"
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
      "enum",
      "required",
      "repeat"
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
      "enum",
      "required",
      "repeat",
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
      "enum",
      "required",
      "repeat",
      "minimum",
      "maximum",
      "format",
      "multipleOf"
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
      "baseUriParameters"
    ),
    "operation" -> Set(
      "displayName",
      "queryParameters",
      "headers",
      "responses",
      "body",
      "protocols",
      "is",
      "securedBy",
      "baseUriParameters"
    )
  )
}

sealed trait RamlSyntax extends SpecSyntax {

  val commonNodes: Map[String, Set[String]] = Map(
    "userDocumentation" -> Set(
      "title",
      "content"
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
    )
  )
}
