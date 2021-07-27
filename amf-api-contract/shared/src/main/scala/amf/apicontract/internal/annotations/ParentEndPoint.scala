package amf.apicontract.internal.annotations

import amf.apicontract.client.scala.model.domain.EndPoint
import amf.core.client.scala.model.domain.{AmfElement, Annotation, AnnotationGraphLoader, ResolvableAnnotation, SerializableAnnotation, UriAnnotation}

case class ParentEndPoint(var parent: Option[EndPoint]) extends SerializableAnnotation with UriAnnotation with ResolvableAnnotation {
  override val name: String  = "parent-end-point"
  override def value: String = parent.map(_.id).getOrElse("default")


  override def uris: Seq[String] = List(value)

  // annotation stores instance so id will be shortened.
  override def shorten(fn: String => String): Annotation = this

  private var parentId: Option[String] = None

  /** To allow deferred resolution on unordered graph parsing. */
  override def resolve(objects: Map[String, AmfElement]): Unit = {
    if (parent.isEmpty && parentId.isDefined) {
      objects.get(parentId.get) match {
        case Some(e: EndPoint) => parent = Some(e)
        case _                 =>
      }
    }
  }
}

object ParentEndPoint extends AnnotationGraphLoader {
  override def unparse(parent: String, objects: Map[String, AmfElement]): Option[ParentEndPoint] = {
    val result = ParentEndPoint(None)
    result.parentId = Some(parent)
    result.resolve(objects)
    Some(result)
  }

  def apply(parent: EndPoint): ParentEndPoint = {
    val result = new ParentEndPoint(Some(parent))
    result.parent = Some(parent)
    result
  }
}
