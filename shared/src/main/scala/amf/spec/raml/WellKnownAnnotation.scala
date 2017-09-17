package amf.spec.raml

import amf.domain.{DomainElement}

object WellKnownAnnotation {

  val annotations: Map[Object,Map[String,Boolean]] = Map(
    "amf.domain.WebApi" -> Map(
      "(termsOfService)"      -> true,
      "(contact)"             -> true,
      "(externalDocs)"        -> true,
      "(license)"             -> true,
      "x-base-uri-parameters" -> true,
      "x-annotationTypes"     -> true
    ),
    "amf.domain.Operation" -> Map(
      "(deprecated)"    -> true,
      "(summary)"       -> true,
      "(externalDocs)"  -> true,
      "(externalDocs)"  -> true
    ),
    "amf.spec.oas.OasParameter" -> Map(
      "x-media-type" -> true
    ),
    "amf.domain.Payload" -> Map(
      "(externalDocs)"   -> true,
      "(readOnly)"       -> true,
      "(dependencies)"   -> true,
      "(tuple)"          -> true,
      "(format)"         -> true,
      "x-media-type"     -> true
    ),
    "amf.domain.Request" -> Map(
      "x-response-payloads" -> true
    ),
    "amf.domain.Response" -> Map(
      "x-request-payloads" -> true
    ),
    "amf.shape.ScalarShape" -> Map(
      "(format)"           -> true,
      "(externalDocs)"     -> true,
      "(exclusiveMaximum)" -> true,
      "(exclusiveMinimum)" -> true
    ),
    "amf.shape.ArrayShape" -> Map (
      "(externalDocs)" -> true,
      "(tuple)"        -> true
    ),
    "amf.shape.MatrixShape" -> Map (
      "(externalDocs)" -> true,
      "(tuple)"        -> true
    ),
    "amf.shape.TupleShape" -> Map (
      "(externalDocs)" -> true,
      "(tuple)"        -> true
    ),
    "amf.shape.NodeShape" -> Map(
      "(externalDocs)" -> true,
      "(readOnly)"     -> true,
      "(dependencies)" -> true
    )
  )

  def normalAnnotation(field: String, model: DomainElement): Boolean = {
    if (field.startsWith("(") && field.endsWith(")")) {
      return annotations.get(model.getClass.getName) match {
        case Some(mapping) => mapping.get(field).isEmpty
        case _             => true
      }
    } else if (field.startsWith("x-") || field.startsWith("X-")) {
      return annotations.get(model.getClass.getName) match {
        case Some(mapping) => mapping.get(field).isEmpty
        case _             => true
      }
    }
    false
  }
  def parseRamlName(s: String): String = s.replace("(", "").replace(")","")
  def parseOasName(s: String): String = s.replace("x-", "").replace("X-","")

  def parseName(s: String) = parseOasName(parseRamlName(s))
}
