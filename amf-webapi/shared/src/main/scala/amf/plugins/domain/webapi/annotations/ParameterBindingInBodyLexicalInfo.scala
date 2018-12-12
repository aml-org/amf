package amf.plugins.domain.webapi.annotations

import amf.core.model.domain._
import amf.core.parser.Range

case class ParameterBindingInBodyLexicalInfo(range: Range) extends SerializableAnnotation with PerpetualAnnotation {
  override val name: String = "parameter-binding-in-body-lexical-info"

  override val value: String = range.toString
}

object ParameterBindingInBodyLexicalInfo extends AnnotationGraphLoader {
  override def unparse(value: String, objects: Map[String, AmfElement]): Option[Annotation] =
    Some(ParameterBindingInBodyLexicalInfo(Range.apply(value)))
}
