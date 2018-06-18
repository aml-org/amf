package amf.plugins.document.webapi.annotations

import amf.core.model.domain.{AmfElement, AnnotationGraphLoader, SerializableAnnotation}

case class ExtensionProvenance(baseId: String, baseLocation: Option[String]) extends SerializableAnnotation {
  override val name: String  = "extension-provenance"
  override val value: String = "id->" + baseId + baseLocation.map(",location->" + _).getOrElse("")
}

object ExtensionProvenance extends AnnotationGraphLoader {
  override def unparse(annotatedValue: String, objects: Map[String, AmfElement]) = {
    annotatedValue.split(",") match {
      case Array(baseId, baseLocation) =>
        ExtensionProvenance(baseId.split("->").last, Option(baseLocation.split("->").last))
      case Array(baseId) => ExtensionProvenance(baseId.split("->").last, None)
    }

  }
}
