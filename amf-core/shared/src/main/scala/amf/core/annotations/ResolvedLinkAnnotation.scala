package amf.core.annotations

import amf.core.model.domain.{AmfElement, AnnotationGraphLoader, SerializableAnnotation}

case class ResolvedLinkAnnotation(linkId: String) extends SerializableAnnotation {
  override val name: String = "resolved-link"
  override val value: String = linkId
}


object ResolvedLinkAnnotation extends AnnotationGraphLoader {
  override def unparse(annotatedValue: String, objects: Map[String, AmfElement]) = {
    ResolvedLinkAnnotation(annotatedValue)
  }
}