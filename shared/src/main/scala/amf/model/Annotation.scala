package amf.model

import amf.parser.Range

/**
  * Created by pedro.colunga on 7/12/17.
  */
trait Annotation

object Annotation {

  case class LexicalInformation(range: Range) extends Annotation

  case class ParentEndPoint(parent: Any) extends Annotation
}
