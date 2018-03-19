package amf.plugins.document.webapi.parser.spec.common

import scala.util.matching.Regex

object WellKnownAnnotation {

  private val ramlKnownAnnotations = Set(
    "termsOfService",
    "parameters",
    "binding",
    "contact",
    "externalDocs",
    "license",
    "base-uri-parameters",
    "deprecated",
    "summary",
    "externalDocs",
    "payloads",
    "request-payloads",
    "response-payloads",
    "media-type",
    "readOnly",
    "dependencies",
    "tuple",
    "format",
    "exclusiveMaximum",
    "exclusiveMinimum",
    "consumes",
    "produces",
    "flow",
    "examples",
    "responses",
    "additionalProperties",
    "collectionFormat",
    "tags",
    "url"
  )

  private val oasKnownAnnotations = Set(
    "base-uri-parameters",
    "annotationTypes",
    "request-payloads",
    "response-payloads",
    "uses",
    "media-type",
    "traits",
    "resourceTypes",
    "is",
    "type",
    "extension-type",
    "fragment-type",
    "usage",
    "title",
    "user-documentation",
    "description",
    "displayName",
    "extends",
    "displayName",
    "describedBy",
    "discriminator-value",
    "requestTokenUri",
    "authorizationUri",
    "tokenCredentialsUri",
    "signatures",
    "settings",
    "securitySchemes",
    "queryParameters",
    "headers",
    "queryString",
    "examples",
    "fileTypes",
    "schema"
  )

  def resolveAnnotation(field: String): Option[String] = {
    field match {
      case ramlAnnotation(value) => Some(value).filterNot(ramlKnownAnnotations.contains)
      case oasAnnotation(value)  => Some(value).filterNot(oasKnownAnnotations.contains).filterNot(_.equals("facets"))
      case _                     => None
    }
  }

  def isOasAnnotation(field: String): Boolean = field match {
    case oasAnnotation(_) => true
    case _                => false
  }

  def isRamlAnnotation(field: String): Boolean = field match {
    case ramlAnnotation(_) => true
    case _                 => false
  }

  private val ramlAnnotation: Regex = "^\\((.+)\\)$".r
  private val oasAnnotation: Regex  = "^[xX]-(.+)".r
}
