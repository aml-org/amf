package amf.maker

import amf.builder.WebApiBuilder
import amf.model.WebApi
import amf.parser.AMFUnit
import amf.remote.{Oas, Raml, Vendor}

/**
  * Domain model WebApi Maker.
  */
class WebApiMaker(unit: AMFUnit) extends Maker[WebApi](unit.vendor) {

  override def make: WebApi = {
    val builder = WebApiBuilder()
    val root    = unit.root.children.head

    vendor match {
      case Oas =>
        builder
          .withName(findValue(root, "info/title"))
          .withDescription(findValue(root, "info/description"))
          .withHost(findValue(root, "host"))
          .withScheme(findValues(root, "schemes"))
          .withBasePath(findValue(root, "basePath"))
          .withAccepts(findValue(root, "consumes"))
          .withContentType(findValue(root, "produces"))
          .withVersion(findValue(root, "info/version"))
          .withTermsOfService(findValue(root, "info/termsOfService"))
      //          .withProvider(findValue(root, "provider"))                TODO use maker
      //          .withLicense(findValue(root, "license"))                  TODO use maker
      //          .withDocumentation(findValue(root, "documentation"))      TODO use maker

      case Raml =>
        builder
          .withName(findValue(root, "title"))
          .withDescription(findValue(root, "description"))
          .withHost(findValue(root, "baseUri")) //TODO extract only domain from baseUri property
          .withScheme(findValues(root, "protocols")) //TODO check if property protocols is empty, look for protocol in the baseUri property
          .withBasePath(findValue(root, "baseUri")) //TODO extract only path from baseUri property
          .withAccepts(findValue(root, "mediaType"))
          .withContentType(findValue(root, "mediaType"))
          .withVersion(findValue(root, "version"))
          .withTermsOfService(findValue(root, "termsOfService"))
//          .withProvider(findValue(root, "provider"))                TODO use maker
//          .withLicense(findValue(root, "license"))                  TODO use maker
//          .withDocumentation(findValue(root, "documentation"))      TODO use maker

      case Vendor(_) =>
    }

    builder.build
  }
}
