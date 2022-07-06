package amf.apicontract.internal.annotations

import amf.core.client.scala.model.domain._
import org.mulesoft.common.client.lexical.PositionRange

case class ParameterBindingInBodyLexicalInfo(range: PositionRange)
    extends SerializableAnnotation
    with PerpetualAnnotation {
  override val name: String = "parameter-binding-in-body-lexical-info"

  override val value: String = range.toString
}

object ParameterBindingInBodyLexicalInfo extends AnnotationGraphLoader {
  override def unparse(value: String, objects: Map[String, AmfElement]): Option[Annotation] =
    Some(ParameterBindingInBodyLexicalInfo(PositionRange(value)))
}
