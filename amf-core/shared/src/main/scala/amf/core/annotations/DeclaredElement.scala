package amf.core.annotations

import amf.core.model.domain.{AmfElement, AnnotationGraphLoader, PerpetualAnnotation, SerializableAnnotation}

case class DeclaredElement() extends SerializableAnnotation with PerpetualAnnotation {
  override val name: String = "declared-element"

  override val value: String = ""
}

object DeclaredElement extends AnnotationGraphLoader {
  override def unparse(annotatedValue: String, objects: Map[String, AmfElement]) = {
    DeclaredElement()
  }
}