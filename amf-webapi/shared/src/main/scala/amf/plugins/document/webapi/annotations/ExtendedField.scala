package amf.plugins.document.webapi.annotations

import amf.core.model.domain.{AmfElement, AnnotationGraphLoader, SerializableAnnotation}

case class ExtendedField(baseId: String) extends  SerializableAnnotation {
  override val name: String  = "extended-field"
  override val value: String = baseId
}

object ExtendedField extends AnnotationGraphLoader {
  override def unparse(annotatedValue: String, objects: Map[String, AmfElement]) = {
    ExtendedField(annotatedValue)
  }
}
