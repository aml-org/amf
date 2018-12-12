package amf.plugins.domain.webapi.annotations

import amf.core.model.domain.{AmfElement, Annotation, AnnotationGraphLoader, SerializableAnnotation}
import amf.plugins.domain.webapi.models.EndPoint

case class ParentEndPoint(parent: EndPoint) extends SerializableAnnotation {
  override val name: String = "parent-end-point"

  override val value: String = parent.id

}

object ParentEndPoint extends AnnotationGraphLoader {
  override def unparse(parent: String, objects: Map[String, AmfElement]): Option[Annotation] =
    objects.get(parent) match {
      case Some(e) => Some(ParentEndPoint(e.asInstanceOf[EndPoint]))
      case _       => None
    }
}
