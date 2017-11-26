package amf.core.annotations

import amf.core.model.domain.{AmfElement, AnnotationGraphLoader, SerializableAnnotation}
import amf.core.parser.Range

case class LexicalInformation(range: Range) extends SerializableAnnotation {
  override val name: String = "lexical"

  override val value: String = range.toString
}

object LexicalInformation extends AnnotationGraphLoader  {
  override def unparse(annotatedValue: String, objects: Map[String, AmfElement]) = {
    LexicalInformation(Range.apply(annotatedValue))
  }
}