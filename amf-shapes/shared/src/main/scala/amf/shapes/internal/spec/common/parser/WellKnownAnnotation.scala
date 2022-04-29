package amf.shapes.internal.spec.common.parser

import scala.util.matching.Regex

/** Annotations that does not exist in a spec but we save them with the 'amf-' prefix and treat them differently than
  * other normal annotations, specially to save information from one spec that can't be expressed in the other
  */
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
    case ramlAnnotation(value) if !isWellKnown(ramlKnownAnnotations, value) => Some(value)
    case oasAnnotation(value) if !isWellKnown(oasKnownAnnotations, value)   => Some(value)
    case _                                                                  => None
  }

  private def isWellKnown(annotations: Set[String], value: String) =
    value.startsWith(amfPrefix) && annotations.contains(value.stripPrefix(amfPrefix))

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
