package amf.domain

import amf.parser.Range

/**
  *
  */
trait Annotation

object Annotation {

  case class LexicalInformation(range: Range) extends Annotation

  case class ParentEndPoint(parent: Any) extends Annotation
}
