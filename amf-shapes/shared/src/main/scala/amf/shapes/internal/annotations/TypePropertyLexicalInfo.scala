package amf.shapes.internal.annotations

import amf.core.client.scala.model.domain._
import org.mulesoft.common.client.lexical.PositionRange

case class TypePropertyLexicalInfo(range: PositionRange) extends SerializableAnnotation with PerpetualAnnotation {
  override val name: String = "type-property-lexical-info"

  override val value: String = range.toString
}

object TypePropertyLexicalInfo extends AnnotationGraphLoader {
  override def unparse(value: String, objects: Map[String, AmfElement]): Option[Annotation] =
    Some(TypePropertyLexicalInfo(PositionRange(value)))
}
