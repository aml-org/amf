package amf.plugins.document.apicontract.parser.spec.common

import scala.util.matching.Regex

object WellKnownAnnotation {

  val ramlKnownAnnotations = Set(
    "baseUriParameters",
    "termsOfService",
    "parameters",
    "binding",
    "contact",
    "externalDocs",
    "license",
    "baseUriParameters",
    "oasDeprecated",
    "summary",
    "defaultResponse",
    "payloads",
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
    "url",
    "serverDescription",
    "servers",
    "xor",
    "and",
    "or",
    "not",
    "minimum",
    "maximum",
    "recursive",
    "pattern",
    "multipleOf",
    "xone",
    "callbacks"
  )

  val oasKnownAnnotations = Set(
    "baseUriParameters",
    "annotationTypes",
    "requestPayloads",
    "responsePayloads",
    "uses",
    "mediaType",
    "traits",
    "resourceTypes",
    "is",
    "type",
    "extensionType",
    "fragmentType",
    "usage",
    "title",
    "userDocumentation",
    "description",
    "displayName",
    "extends",
    "describedBy",
    "discriminatorValue",
    "settings",
    "securitySchemes",
    "queryParameters",
    "queryString",
    "examples",
    "fileTypes",
    "schema",
    "serverDescription",
    "servers",
    "consumes",
    "produces",
    "schemes",
    "parameters",
    "facets",
    "merge",
    "union",
    "security",
    "required",
    "example",
    "examples"
  )

  def resolveAnnotation(field: String): Option[String] = field match {
    case ramlAnnotation(value) if notContains(ramlKnownAnnotations, value) => Some(value)
    case oasAnnotation(value) if notContains(oasKnownAnnotations, value)   => Some(value)
    case _                                                                 => None
  }

  private def notContains(annotations: Set[String], value: String) =
    !annotations.contains(value.stripPrefix(amfPrefix))

  def isOasAnnotation(field: String): Boolean = field match {
    case oasAnnotation(_) => true
    case _                => false
  }

  def isRamlAnnotation(field: String): Boolean = field match {
    case ramlAnnotation(_) => true
    case _                 => false
  }

  private val amfPrefix             = "amf-"
  private val ramlAnnotation: Regex = "^\\((.+)\\)$".r
  private val oasAnnotation: Regex  = "^[xX]-(.+)".r
}
