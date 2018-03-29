package amf.core.annotations

import amf.core.model.domain.{AmfElement, AnnotationGraphLoader, SerializableAnnotation}
import amf.core.remote._

case class SourceVendor(vendor: Vendor) extends SerializableAnnotation {
  override val name: String = "source-vendor"

  override val value: String = vendor.name
}

object SourceVendor extends AnnotationGraphLoader {
  def apply(vendor: String): SourceVendor = vendor match {
    case Raml.name                => SourceVendor(Raml)
    case Raml08.name | "RAML 0.8" => SourceVendor(Raml08)
    case Raml10.name | "RAML 1.0" => SourceVendor(Raml10)
    case Oas3.name                => SourceVendor(Oas3)
    case Oas.name                 => SourceVendor(Oas)
    case Amf.name                 => SourceVendor(Amf)
    case "OAS 3.0.0"              => SourceVendor(Oas3)
    case "OAS 2.0"                => SourceVendor(Oas)
    case "AMF Graph"              => SourceVendor(Amf)
    case _                        => SourceVendor(Unknown)
  }

  override def unparse(annotatedValue: String, objects: Map[String, AmfElement]) = {
    annotatedValue match {
      case Vendor(vendor) => SourceVendor(vendor)
      case _              => throw new RuntimeException(s"Illegal vendor: '$annotatedValue'")
    }
  }
}
