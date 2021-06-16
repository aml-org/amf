package amf.apicontract.internal.spec.raml.parser

object Raml10Syntax extends RamlSyntax {

  private val shapeFacets = Set(
    "type",
    "default",
    "schema",
    "example",
    "examples",
    "displayName",
    "description",
    "facets",
    "xml",
    "enum"
  )

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
    "shape"    -> shapeFacets,
    "anyShape" -> shapeFacets,
    "schemaShape" -> Set(
      "type",
      "default",
      "schema",
      "example",
      "examples",
      "displayName",
      "description",
      "required"
    ),
    "unionShape" -> (shapeFacets + "anyOf"),
    "nodeShape" -> (shapeFacets ++ Set(
      "properties",
      "minProperties",
      "maxProperties",
      "discriminator",
      "discriminatorValue",
      "additionalProperties"
    )),
    "arrayShape" -> (shapeFacets ++ Set(
      "uniqueItems",
      "items",
      "minItems",
      "maxItems"
    )),
    "stringScalarShape" -> (shapeFacets ++ Set(
      "pattern",
      "minLength",
      "maxLength"
    )),
    "numberScalarShape" -> (shapeFacets ++ Set(
      "minimum",
      "maximum",
      "format",
      "multipleOf"
    )),
    "dateScalarShape" -> (shapeFacets + "format"),
    "fileShape" -> (shapeFacets ++ Set(
      "fileTypes",
      "minLength",
      "maxLength"
    )),
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
      "connect",
      "trace",
      "get?",
      "patch?",
      "put?",
      "post?",
      "delete?",
      "options?",
      "head?",
      "connect?",
      "trace?",
      "is",
      "type",
      "securedBy",
      "uriParameters",
      "usage"
    ),
    "resourceType" -> Set(
      "displayName",
      "description",
      "get",
      "patch",
      "put",
      "post",
      "delete",
      "options",
      "head",
      "connect",
      "trace",
      "get?",
      "patch?",
      "put?",
      "post?",
      "delete?",
      "options?",
      "head?",
      "connect?",
      "trace?",
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
    "trait" -> Set(
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
    ),
    "property" -> Set(
      "required"
    ),
    "module" -> Set(
      "uses",
      "usage",
      "types",
      "schemas",
      "resourceTypes",
      "traits",
      "securitySchemes",
      "annotationTypes"
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
      "repeat",
      "pattern",
      "minLength",
      "maxLength",
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
      "enum",
      "required",
      "repeat",
      "format"
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
      "connect",
      "trace",
      "get?",
      "patch?",
      "put?",
      "post?",
      "delete?",
      "options?",
      "head?",
      "connect?",
      "trace?",
      "is",
      "type",
      "securedBy",
      "baseUriParameters",
      "uriParameters",
      "usage"
    ),
    "resourceType" -> Set(
      "displayName",
      "description",
      "get",
      "patch",
      "put",
      "post",
      "delete",
      "options",
      "head",
      "connect",
      "trace",
      "get?",
      "patch?",
      "put?",
      "post?",
      "delete?",
      "options?",
      "head?",
      "connect?",
      "trace?",
      "is",
      "type",
      "securedBy",
      "baseUriParameters",
      "uriParameters",
      "usage",
      "is?",
      "securedBy?",
      "baseUriParameters?",
      "uriParameters?"
    ),
    "operation" -> Set(
      "displayName",
      "description",
      "queryParameters",
      "headers",
      "responses",
      "body",
      "protocols",
      "is",
      "securedBy",
      "baseUriParameters",
      "usage"
    ),
    "trait" -> Set(
      "displayName",
      "description",
      "queryParameters",
      "headers",
      "responses",
      "body",
      "protocols",
      "is",
      "securedBy",
      "baseUriParameters",
      "usage",
      "queryParameters?",
      "headers?",
      "responses?",
      "body?",
      "protocols?",
      "is?",
      "securedBy?",
      "baseUriParameters?"
    ),
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
      "describedBy",
      "settings"
    ),
    "describedBy" -> Set(
      "headers",
      "queryParameters",
      "queryString",
      "responses"
    )
  )
}
