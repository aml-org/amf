package amf.core.annotations

import amf.core.model.domain._

case class ExternalFragmentRef(fragment: String) extends SerializableAnnotation with PerpetualAnnotation {
  override val name: String  = "external-fragment-ref"
  override val value: String = fragment
}

object ExternalFragmentRef extends AnnotationGraphLoader {
  override def unparse(value: String, objects: Map[String, AmfElement]): Option[Annotation] =
    Some(ExternalFragmentRef(value))
}