package amf.plugins.document.vocabularies.annotations

import amf.core.model.domain.{AmfElement, AnnotationGraphLoader, SerializableAnnotation}

case class CustomId() extends  SerializableAnnotation {
  override val name: String = "custom-id"
  override val value: String = "true"
}

object CustomId extends  AnnotationGraphLoader {
  override def unparse(annotationValue: String, objects: Map[String, AmfElement]) = CustomId()
}
