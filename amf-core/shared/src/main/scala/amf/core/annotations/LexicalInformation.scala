package amf.core.annotations

import amf.core.model.domain.{AmfElement, AnnotationGraphLoader, PerpetualAnnotation, SerializableAnnotation}
import amf.core.parser.Range

case class LexicalInformation(range: Range) extends SerializableAnnotation with PerpetualAnnotation {
  override val name: String = "lexical"

  override val value: String = range.toString
}

object LexicalInformation extends AnnotationGraphLoader {
  override def unparse(annotatedValue: String, objects: Map[String, AmfElement]) = {
    LexicalInformation.apply(annotatedValue)
  }

  def apply(range: String): LexicalInformation = new LexicalInformation(Range.apply(range))
}
