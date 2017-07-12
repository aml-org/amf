package amf.maker

import amf.model.Organization
import amf.parser.ASTNode
import amf.remote.{Oas, Raml, Vendor}

/**
  * Domain model Organization Maker.
  */
class OrganizationMaker(node: ASTNode[_], vendor: Vendor) extends Maker[Organization](vendor) {
  override def make: Organization = {
    val builder = builders.organization

    vendor match {
      case Raml =>
        builder
          .withUrl(findValue(node, "contact", "url"))
          .withName(findValue(node, "contact", "name"))
          .withEmail(findValue(node, "contact", "email"))
      case Oas =>
        builder
          .withUrl(findValue(node, "contact", "url"))
          .withName(findValue(node, "contact", "name"))
          .withEmail(findValue(node, "contact", "email"))
      case Vendor(_) =>
    }

    builder.build
  }
}

object OrganizationMaker {
  def apply(node: ASTNode[_], vendor: Vendor): OrganizationMaker = new OrganizationMaker(node, vendor)
}
