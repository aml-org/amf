package amf.core.annotations

import amf.core.model.domain._

case class DeclaredElement() extends SerializableAnnotation with PerpetualAnnotation {
  override val name: String = "declared-element"

  override val value: String = ""
}

object DeclaredElement extends AnnotationGraphLoader {
  override def unparse(value: String, objects: Map[String, AmfElement]): Option[Annotation] = Some(DeclaredElement())
}