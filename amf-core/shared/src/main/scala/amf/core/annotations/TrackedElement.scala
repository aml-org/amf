package amf.core.annotations

import amf.core.model.domain.{AmfElement, AnnotationGraphLoader, EternalSerializedAnnotation}

case class TrackedElement(parent: String) extends EternalSerializedAnnotation {

  /** Extension name. */
  override val name: String = "tracked-element"

  /** Value as string. */
  override val value: String = parent
}

object TrackedElement extends AnnotationGraphLoader {
  override def unparse(annotatedValue: String, objects: Map[String, AmfElement]) = {
    TrackedElement(annotatedValue)
  }
}
