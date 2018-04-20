package amf.plugins.domain.webapi.annotations

import amf.core.model.domain.{AmfElement, AnnotationGraphLoader, PerpetualAnnotation, SerializableAnnotation}
import amf.core.parser.Range

case class ParameterBindingInBodyLexicalInfo(range: Range) extends SerializableAnnotation with PerpetualAnnotation {
  override val name: String = "parameter-binding-in-body-lexical-info"

  override val value: String = range.toString
}

object ParameterBindingInBodyLexicalInfo extends AnnotationGraphLoader {
  override def unparse(annotatedValue: String, objects: Map[String, AmfElement]): ParameterBindingInBodyLexicalInfo = {
    ParameterBindingInBodyLexicalInfo(Range.apply(annotatedValue))
  }
}
