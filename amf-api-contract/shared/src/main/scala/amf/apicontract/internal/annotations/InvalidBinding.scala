package amf.apicontract.internal.annotations

import amf.core.client.scala.model.domain.{AmfElement, Annotation, AnnotationGraphLoader, SerializableAnnotation}

case class InvalidBinding(value: String) extends SerializableAnnotation {
  override val name: String = "invalid-binding"
}

object InvalidBinding extends AnnotationGraphLoader {
  override def unparse(value: String, objects: Map[String, AmfElement]): Option[Annotation] =
    Some(InvalidBinding(value))
}
