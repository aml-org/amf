package amf.plugins.domain.apicontract.annotations

import amf.core.model.domain._
import amf.plugins.domain.apicontract.models.EndPoint

case class ParentEndPoint(reference: String) extends SerializableAnnotation with ResolvableAnnotation {
  override val name: String  = "parent-end-point"
  override val value: String = reference

  var parent: Option[EndPoint] = None

  /** To allow deferred resolution on unordered graph parsing. */
  override def resolve(objects: Map[String, AmfElement]): Unit = {
    if (parent.isEmpty) {
      objects.get(reference) match {
        case Some(e: EndPoint) => parent = Some(e)
        case _                 =>
      }
    }
  }
}

object ParentEndPoint extends AnnotationGraphLoader {
  override def unparse(parent: String, objects: Map[String, AmfElement]): Option[ParentEndPoint] = {
    val result = ParentEndPoint(parent)
    result.resolve(objects)
    Some(result)
  }

  def apply(parent: EndPoint): ParentEndPoint = {
    val result = new ParentEndPoint(parent.id)
    result.parent = Some(parent)
    result
  }
}
