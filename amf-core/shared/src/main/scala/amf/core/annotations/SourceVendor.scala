package amf.core.annotations

import amf.core.model.domain._
import amf.core.remote._

case class SourceVendor(vendor: Vendor) extends SerializableAnnotation with PerpetualAnnotation {
  override val name: String = "source-vendor"

  override val value: String = vendor.name
}

object SourceVendor extends AnnotationGraphLoader {
  def parse(vendor: String): Option[SourceVendor] = vendor match {
    case Raml.name   => Some(SourceVendor(Raml))
    case Raml08.name => Some(SourceVendor(Raml08))
    case Raml10.name => Some(SourceVendor(Raml10))
    case Amf.name    => Some(SourceVendor(Amf))
    case Oas.name    => Some(SourceVendor(Oas))
    case Oas20.name  => Some(SourceVendor(Oas))
    case Oas30.name  => Some(SourceVendor(Oas30))
    case _           => None
  }

  override def unparse(value: String, objects: Map[String, AmfElement]): Option[Annotation] =
    SourceVendor.parse(value)
}
