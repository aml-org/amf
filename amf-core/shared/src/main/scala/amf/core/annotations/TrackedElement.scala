package amf.core.annotations

import amf.core.model.domain._

case class TrackedElement(parent: String) extends EternalSerializedAnnotation with UriAnnotation {

  /** Extension name. */
  override val name: String = "tracked-element"

  /** Value as string. */
  override val value: String     = parent
  override val uris: Seq[String] = Seq(parent)

  override def shorten(fn: String => String): Annotation = TrackedElement(fn(parent))
}

object TrackedElement extends AnnotationGraphLoader {
  override def unparse(value: String, objects: Map[String, AmfElement]): Option[Annotation] =
    Some(TrackedElement(value))
}
