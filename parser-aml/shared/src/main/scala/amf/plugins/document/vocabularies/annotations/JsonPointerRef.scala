package amf.plugins.document.vocabularies.annotations

import amf.core.model.domain.{AmfElement, AnnotationGraphLoader, SerializableAnnotation}

case class JsonPointerRef() extends  SerializableAnnotation {
  override val name: String = "json-pointer-ref"
  override val value: String = "true"
}

object JsonPointerRef extends  AnnotationGraphLoader {
  override def unparse(annotationValue: String, objects: Map[String, AmfElement]) = JsonPointerRef()
}
