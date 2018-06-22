package amf.plugins.document.vocabularies.annotations

import amf.core.model.domain.{AmfElement, AnnotationGraphLoader, SerializableAnnotation}

case class RefInclude() extends  SerializableAnnotation {
  override val name: String = "ref-include"
  override val value: String = "true"
}

object RefInclude extends  AnnotationGraphLoader {
  override def unparse(annotationValue: String, objects: Map[String, AmfElement]) = RefInclude()
}
