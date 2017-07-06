package amf.maker

import amf.builder.BaseWebApiBuilder
import amf.model.WebApiModel
import amf.parser.AMFUnit
import amf.remote.{Amf, Oas, Raml, Vendor}
import amf.unsafe.BuilderFactory.webApiBuilder

/**
  * Domain model WebApi Maker.
  */
class WebApiMaker(unit: AMFUnit) extends Maker[WebApiModel](unit.vendor) {

  override def make: WebApiModel = {
    val builder: BaseWebApiBuilder = webApiBuilder
    val root                       = unit.root.children.head

    vendor match {
      case Oas =>
        builder
          .withName(findValue(root, "info", "title"))
          .withDescription(findValue(root, "info", "description"))
          .withHost(findValue(root, "host"))
          .withScheme(findValues(root, "schemes"))
          .withBasePath(findValue(root, "basePath"))
          .withAccepts(findValue(root, "consumes"))
          .withContentType(findValue(root, "produces"))
          .withVersion(findValue(root, "info", "version"))
          .withTermsOfService(findValue(root, "info", "termsOfService"))

      case Raml =>
        val urls          = BaseUriSplitter(findValue(root, "baseUri"))
        val protocolsList = findValues(root, "protocols")
        builder
          .withName(findValue(root, "title"))
          .withDescription(findValue(root, "description"))
          .withHost(urls.url())
          .withScheme(if (protocolsList.isEmpty) List(urls.protocol) else protocolsList)
          .withBasePath(urls.path)
          .withAccepts(findValue(root, "mediaType"))
          .withContentType(findValue(root, "mediaType"))
          .withVersion(findValue(root, "version"))
          .withTermsOfService(findValue(root, "termsOfService"))
      case Amf =>
        builder
          .withName(
            findValue(root, "http://raml.org/vocabularies/document#encodes", "http://schema.org/name", "@value"))
          .withHost(
            findValue(root,
                      "http://raml.org/vocabularies/document#encodes",
                      "http://raml.org/vocabularies/http#host",
                      "@value"))
          .withScheme(
            findValues(root,
                       "http://raml.org/vocabularies/document#encodes",
                       "http://raml.org/vocabularies/http#scheme",
                       "@value"))
      case Vendor(_) =>
    }

    builder
      .withProvider(OrganizationMaker(root, vendor).make)
      .withLicense(LicenseMaker(root, vendor).make)
      .withDocumentation(CreativeWorkMaker(root, vendor).make)
      .build
  }
}

object WebApiMaker {
  def apply(unit: AMFUnit): WebApiMaker = new WebApiMaker(unit)
}
