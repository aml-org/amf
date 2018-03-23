package amf.plugins.domain.shapes.annotations

import amf.core.model.domain.{AmfElement, AnnotationGraphLoader, SerializableAnnotation}

case class InheritedField(baseId: String) extends  SerializableAnnotation {
  override val name: String  = "inherited-field"
  override val value: String = baseId
}

object InheritedField extends AnnotationGraphLoader {
  override def unparse(annotatedValue: String, objects: Map[String, AmfElement]) = {
    InheritedField(annotatedValue)
  }
}
