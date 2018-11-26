package amf.core.annotations

import amf.core.model.domain.{AmfElement, AnnotationGraphLoader, PerpetualAnnotation, SerializableAnnotation}
import amf.core.remote._

case class SourceVendor(vendor: Vendor) extends SerializableAnnotation with PerpetualAnnotation {
  override val name: String = "source-vendor"

  override val value: String = vendor.name
}

object SourceVendor extends AnnotationGraphLoader {
  def apply(vendor: String): SourceVendor = vendor match {
    case Raml.name                 => SourceVendor(Raml)
    case Raml08.name | Raml08.name => SourceVendor(Raml08)
    case Raml10.name | Raml10.name => SourceVendor(Raml10)
    case Amf.name                  => SourceVendor(Amf)
    case Oas.name                  => SourceVendor(Oas)
    case Oas20.name                => SourceVendor(Oas)
    case Oas30.name                => SourceVendor(Oas30)
    case _                         => SourceVendor(Amf) // todo: default?
  }

  override def unparse(annotatedValue: String, objects: Map[String, AmfElement]) = {
    annotatedValue match {
      case Vendor(vendor) => SourceVendor(vendor)
      case _              => SourceVendor(Vendor(annotatedValue))
    }
  }
}
