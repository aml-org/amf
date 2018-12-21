package amf.plugins.domain.webapi.annotations

import amf.core.model.domain.{AmfElement, Annotation, AnnotationGraphLoader, SerializableAnnotation}

case class OrphanOasExtension(location: String) extends SerializableAnnotation {
  override val name: String  = "orphan-oas-extension"
  override val value: String = location
}

object OrphanOasExtension extends AnnotationGraphLoader {
  override def unparse(value: String, objects: Map[String, AmfElement]): Option[Annotation] =
    Some(OrphanOasExtension(value))
}
