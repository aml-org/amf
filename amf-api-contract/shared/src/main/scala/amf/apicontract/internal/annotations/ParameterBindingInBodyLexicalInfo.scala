package amf.apicontract.internal.annotations

import amf.core.client.common.position.Range
import amf.core.client.scala.model.domain._

case class ParameterBindingInBodyLexicalInfo(range: Range) extends SerializableAnnotation with PerpetualAnnotation {
  override val name: String = "parameter-binding-in-body-lexical-info"

  override val value: String = range.toString
}

object ParameterBindingInBodyLexicalInfo extends AnnotationGraphLoader {
  override def unparse(value: String, objects: Map[String, AmfElement]): Option[Annotation] =
    Some(ParameterBindingInBodyLexicalInfo(Range.apply(value)))
}
