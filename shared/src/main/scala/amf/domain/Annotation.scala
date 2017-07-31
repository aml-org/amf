package amf.domain

import amf.parser.Range

/**
  *
  */
trait Annotation

object Annotation {

  case class LexicalInformation(range: Range) extends Annotation

  case class ParentEndPoint(parent: EndPoint) extends Annotation

  case class ExplicitField() extends Annotation

  case class UriParameter() extends Annotation

  case class ArrayFieldAnnotations(holder: Map[Any, List[Annotation]] = Map()) extends Annotation {
    def +(fieldValue: Any, annotations: List[Annotation]) = ArrayFieldAnnotations(holder + (fieldValue -> annotations))

    def apply(fieldValue: Any): List[Annotation] = holder.getOrElse(fieldValue, Nil)
  }
}
