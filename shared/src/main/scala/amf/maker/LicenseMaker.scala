package amf.maker

import amf.model.License
import amf.parser.ASTNode
import amf.remote.{Oas, Raml, Vendor}
import amf.unsafe.BuilderFactory.licenseBuilder

/**
  * Created by martin.gutierrez on 6/29/17.
  */
/**
  * Domain model Organization Maker.
  */
class LicenseMaker(node: ASTNode[_], vendor: Vendor) extends Maker[License](vendor) {
  override def make: License = {
    val builder = licenseBuilder

    vendor match {
      case Raml =>
        builder
          .withUrl(findValue(node, "license", "url"))
          .withName(findValue(node, "license", "name"))
      case Oas =>
        builder
          .withUrl(findValue(node, "info", "license", "url"))
          .withName(findValue(node, "info", "license", "name"))
      case Vendor(_) =>
    }

    builder.build
  }
}

object LicenseMaker {
  def apply(node: ASTNode[_], vendor: Vendor): LicenseMaker = new LicenseMaker(node, vendor)
}
