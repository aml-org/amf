package amf.plugins.domain.shapes.annotations

import amf.core.model.domain.{AmfElement, Annotation, AnnotationGraphLoader, SerializableAnnotation}

object ParsedFromTypeExpression extends AnnotationGraphLoader {
  override def unparse(value: String, objects: Map[String, AmfElement]): Option[Annotation] =
    Some(ParsedFromTypeExpression(value))
}

case class ParsedFromTypeExpression(expression: String) extends SerializableAnnotation {
  override val name: String  = "type-expression"
  override val value: String = expression
}