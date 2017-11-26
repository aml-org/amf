package amf.plugins.domain.webapi.annotations

import amf.core.model.domain.{AmfElement, AnnotationGraphLoader, SerializableAnnotation}
import amf.plugins.domain.webapi.models.EndPoint

case class ParentEndPoint(parent: EndPoint) extends SerializableAnnotation {
  override val name: String = "parent-end-point"

  override val value: String = parent.id

}

object ParentEndPoint extends AnnotationGraphLoader  {
  override def unparse(annotatedValue: String, objects: Map[String, AmfElement]) = {
    ParentEndPoint(objects(annotatedValue).asInstanceOf[EndPoint])
  }
}
