package amf.core.annotations

import amf.core.model.domain.{AmfElement, AnnotationGraphLoader, SerializableAnnotation}
import amf.core.remote.{Amf, Oas, Raml, Unknown, Vendor}

case class SourceVendor(vendor: Vendor) extends SerializableAnnotation {
  override val name: String = "source-vendor"

  override val value: String = vendor.name
}

object SourceVendor extends AnnotationGraphLoader {
  def apply(vendor: String): SourceVendor = vendor match {
    case Raml.name => SourceVendor(Raml)
    case Oas.name  => SourceVendor(Oas)
    case Amf.name  => SourceVendor(Amf)
    case "RAML 1.0" => SourceVendor(Raml)
    case "OAS 2.0" => SourceVendor(Oas)
    case "AMF Graph" => SourceVendor(Amf)
    case _         => SourceVendor(Unknown)
  }

  override def unparse(annotatedValue: String, objects: Map[String, AmfElement]) = {
    annotatedValue match {
      case Vendor(vendor) => SourceVendor(vendor)
      case _              => throw new RuntimeException(s"Illegal vendor: '$annotatedValue'")
    }
  }
}
