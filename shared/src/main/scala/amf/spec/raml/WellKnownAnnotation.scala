package amf.spec.raml

import amf.domain.{DomainElement, Operation, WebApi}
import amf.shape._

object WellKnownAnnotation {

  val annotations: Map[Object,Map[String,Boolean]] = Map(
    "amf.domain.WebApi" -> Map(
      "(termsOfService)" -> true,
      "(contact)"        -> true,
      "(externalDocs)"   -> true,
      "(license)"        -> true
    ),
    "amf.domain.Operation" -> Map(
      "(deprecated)"   -> true,
      "(summary)"      -> true,
      "(externalDocs)" -> true,
      "(externalDocs)"   -> true
    ),
    "amf.domain.ScalarShape" -> Map(
      "(format)"           -> true,
      "(externalDocs)"   -> true,
      "(exclusiveMaximum)" -> true,
      "(exclusiveMinimum)" -> true
    ),
    "amf.domain.ArrayShape" -> Map (
      "(externalDocs)"   -> true,
      "(tuple)" -> true
    ),
    "amf.domain.MatrixShape" -> Map (
      "(externalDocs)"   -> true,
      "(tuple)" -> true
    ),
    "amf.domain.TupleShape" -> Map (
      "(externalDocs)"   -> true,
      "(tuple)" -> true
    ),
    "amf.domain.NodeShape" -> Map(
      "(externalDocs)"   -> true,
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
    }
    false
  }
  def parseRamlName(s: String): String = s.replace("(", "").replace(")","")
  def parseOasName(s: String): String = s.replace("x-", "")
}
