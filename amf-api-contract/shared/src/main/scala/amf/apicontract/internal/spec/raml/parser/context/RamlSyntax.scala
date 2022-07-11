package amf.apicontract.internal.spec.raml.parser.context

import amf.shapes.internal.spec.common.parser.SpecSyntax
import amf.shapes.internal.spec.raml.parser.{Raml08ShapeSyntax, Raml10ShapeSyntax}

object Raml10Syntax extends RamlSyntax {

  override val nodes: Map[String, Set[String]] = commonNodes ++ Raml10ShapeSyntax.nodes ++ Map(
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

  override val nodes: Map[String, Set[String]] = commonNodes ++ Raml08ShapeSyntax.nodes ++ Map(
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
