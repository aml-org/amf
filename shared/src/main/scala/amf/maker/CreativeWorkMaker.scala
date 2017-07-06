package amf.maker

import amf.model.CreativeWork
import amf.parser.ASTNode
import amf.remote.{Oas, Raml, Vendor}
import amf.unsafe.BuilderFactory.creativeWorkBuilder

/**
  * Domain model Organization Maker.
  */
class CreativeWorkMaker(node: ASTNode[_], vendor: Vendor) extends Maker[CreativeWork](vendor) {
  override def make: CreativeWork = {
    val builder = creativeWorkBuilder

    vendor match {
      case Raml =>
        builder
          .withUrl(findValue(node, "externalDocs", "url"))
          .withDescription(findValue(node, "externalDocs", "description"))
      case Oas =>
        builder
          .withUrl(findValue(node, "externalDocs", "url"))
          .withDescription(findValue(node, "externalDocs", "description"))
      case Vendor(_) =>
    }

    builder.build
  }
}

object CreativeWorkMaker {
  def apply(node: ASTNode[_], vendor: Vendor): CreativeWorkMaker = new CreativeWorkMaker(node, vendor)
}
