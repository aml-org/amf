package amf.plugins.document.webapi.annotations

import amf.core.model.domain.{AmfElement, AnnotationGraphLoader, SerializableAnnotation}

case class ExtensionProvenance(baseId: String) extends  SerializableAnnotation {
  override val name: String  = "extension-provenance"
  override val value: String = baseId
}

object ExtensionProvenance extends AnnotationGraphLoader {
  override def unparse(annotatedValue: String, objects: Map[String, AmfElement]) = {
    ExtensionProvenance(annotatedValue)
  }
}
