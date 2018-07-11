package amf.core.annotations

import amf.core.model.domain.{AmfElement, AnnotationGraphLoader, PerpetualAnnotation, SerializableAnnotation}

case class ExternalFragmentRef(fragment: String) extends SerializableAnnotation with PerpetualAnnotation {
  override val name: String  = "external-fragment-ref"
  override val value: String = fragment
}

object ExternalFragmentRef extends AnnotationGraphLoader {
  override def unparse(annotatedValue: String, objects: Map[String, AmfElement]) = {
    ExternalFragmentRef(annotatedValue)
  }
}