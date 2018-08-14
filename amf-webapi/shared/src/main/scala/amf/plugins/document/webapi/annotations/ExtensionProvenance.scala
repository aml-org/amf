package amf.plugins.document.webapi.annotations

import amf.core.model.domain._

case class ExtensionProvenance(baseId: String, baseLocation: Option[String])
    extends SerializableAnnotation
    with UriAnnotation
    with PerpetualAnnotation {
  override val name: String      = "extension-provenance"
  override val value: String     = "id->" + baseId + baseLocation.map(",location->" + _).getOrElse("")
  override val uris: Seq[String] = Seq(baseId)

  override def shorten(fn: String => String): Annotation = ExtensionProvenance(fn(baseId), baseLocation)
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
