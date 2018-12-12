package amf.plugins.domain.shapes.annotations

import amf.core.model.domain._

case class InheritanceProvenance(baseId: String)
    extends SerializableAnnotation
    with PerpetualAnnotation
    with UriAnnotation {
  override val name: String      = "inheritance-provenance"
  override val value: String     = baseId
  override val uris: Seq[String] = Seq(baseId)

  override def shorten(fn: String => String): Annotation = InheritanceProvenance(fn(baseId))
}

object InheritanceProvenance extends AnnotationGraphLoader {
  override def unparse(value: String, objects: Map[String, AmfElement]): Option[Annotation] =
    Some(InheritanceProvenance(value))
}

case class InheritedShapes(baseIds: Seq[String])
    extends SerializableAnnotation
    with PerpetualAnnotation
    with UriAnnotation {
  override val name: String      = "inherited-shapes"
  override val value: String     = baseIds.mkString(",")
  override val uris: Seq[String] = baseIds

  override def shorten(fn: String => String): Annotation = InheritedShapes(baseIds.map(fn))
}

object InheritedShapes extends AnnotationGraphLoader {
  override def unparse(value: String, objects: Map[String, AmfElement]): Option[Annotation] =
    Some(InheritedShapes(value.split(",")))
}

case class NilUnion(position: String) extends SerializableAnnotation {
  override val name: String  = "nil-union"
  override val value: String = position
}

object NilUnion extends AnnotationGraphLoader {
  override def unparse(annotatedValue: String, objects: Map[String, AmfElement]): Option[Annotation] =
    Some(NilUnion(annotatedValue))
}
