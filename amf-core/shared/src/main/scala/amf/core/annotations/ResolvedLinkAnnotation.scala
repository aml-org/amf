package amf.core.annotations

import amf.core.model.domain._

case class ResolvedLinkAnnotation(linkId: String)
    extends SerializableAnnotation
    with PerpetualAnnotation
    with UriAnnotation {

  override val name: String                              = "resolved-link"
  override val value: String                             = linkId
  override val uris: Seq[String]                         = Seq(linkId)
  override def shorten(fn: String => String): Annotation = ResolvedLinkAnnotation(fn(linkId))
}

object ResolvedLinkAnnotation extends AnnotationGraphLoader {
  override def unparse(annotatedValue: String, objects: Map[String, AmfElement]) = {
    ResolvedLinkAnnotation(annotatedValue)
  }
}

case class ResolvedInheritance() extends PerpetualAnnotation
