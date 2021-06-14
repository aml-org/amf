package amf.plugins.domain.apicontract.annotations

import amf.core.client.scala.model.domain.{
  AmfElement,
  Annotation,
  AnnotationGraphLoader,
  PerpetualAnnotation,
  SerializableAnnotation
}
import amf.core.client.common.position.Range

case class TypePropertyLexicalInfo(range: Range) extends SerializableAnnotation with PerpetualAnnotation {
  override val name: String = "type-property-lexical-info"

  override val value: String = range.toString
}

object TypePropertyLexicalInfo extends AnnotationGraphLoader {
  override def unparse(value: String, objects: Map[String, AmfElement]): Option[Annotation] =
    Some(TypePropertyLexicalInfo(Range.apply(value)))
}
