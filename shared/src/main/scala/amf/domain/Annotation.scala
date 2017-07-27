package amf.domain

import amf.metadata.Field
import amf.parser.Range

/**
  *
  */
trait Annotation

object Annotation {

  case class LexicalInformation(range: Range) extends Annotation

  case class ParentEndPoint(parent: EndPoint) extends Annotation

  case class ExplicitField(field: Field) extends Annotation

  case class UriParameter() extends Annotation

  case class ArrayFieldAnnotations(holder: Map[Any, List[Annotation]] = Map()) extends Annotation {
    def +(fieldValue: Any, annotations: List[Annotation]) = ArrayFieldAnnotations(holder + (fieldValue -> annotations))
  }
}
