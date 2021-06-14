package amf.plugins.domain.apicontract.annotations

import amf.core.client.scala.model.domain.{
  AmfElement,
  Annotation,
  AnnotationGraphLoader,
  PerpetualAnnotation,
  SerializableAnnotation
}

case class ParameterBindingInBodyLexicalInfo(range: Range) extends SerializableAnnotation with PerpetualAnnotation {
  override val name: String = "parameter-binding-in-body-lexical-info"

  override val value: String = range.toString
}

object ParameterBindingInBodyLexicalInfo extends AnnotationGraphLoader {
  override def unparse(value: String, objects: Map[String, AmfElement]): Option[Annotation] =
    Some(ParameterBindingInBodyLexicalInfo(Range.apply(value)))
}
