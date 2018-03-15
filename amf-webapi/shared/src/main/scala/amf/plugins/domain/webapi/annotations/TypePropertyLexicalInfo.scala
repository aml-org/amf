package amf.plugins.domain.webapi.annotations

import amf.core.model.domain.{AmfElement, AnnotationGraphLoader, PerpetualAnnotation, SerializableAnnotation}
import amf.core.parser.Range

case class TypePropertyLexicalInfo(range: Range) extends SerializableAnnotation with PerpetualAnnotation {
  override val name: String = "type-property-lexical-info"

  override val value: String = range.toString
}

object TypePropertyLexicalInfo extends AnnotationGraphLoader {
  override def unparse(annotatedValue: String, objects: Map[String, AmfElement]) = {
    TypePropertyLexicalInfo(Range.apply(annotatedValue))
  }
}
