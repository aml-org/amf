package amf.model.builder

import amf.model._

import scala.collection.JavaConverters._

/**
  * Builder class for web api jvm
  */
case class WebApiBuilder private (private val webApiBuilder: amf.builder.WebApiBuilder = amf.builder.WebApiBuilder())
    extends Builder {

  def this() = { this(amf.builder.WebApiBuilder()) }

  def withName(name: String): WebApiBuilder = {
    webApiBuilder.withName(name)
    this
  }

  def withDescription(description: String): WebApiBuilder = {
    webApiBuilder.withDescription(description)
    this
  }

  def withHost(host: String): WebApiBuilder = {
    webApiBuilder.withHost(host)
    this
  }

  def withSchemes(schemes: java.util.List[String]): WebApiBuilder = {
    webApiBuilder.withSchemes(schemes.asScala.toList)
    this
  }

  def withEndPoints(endPoints: java.util.List[EndPoint]): WebApiBuilder = {
    webApiBuilder.withEndPoints(endPoints.asScala.map(_.element).toList)
    this
  }

  def withBasePath(path: String): WebApiBuilder = {
    webApiBuilder.withBasePath(path)
    this
  }

  def withAccepts(accepts: java.util.List[String]): WebApiBuilder = {
    webApiBuilder.withAccepts(accepts.asScala.toList)
    this
  }

  def withContentType(contentType: java.util.List[String]): WebApiBuilder = {
    webApiBuilder.withContentType(contentType.asScala.toList)
    this
  }

  def withVersion(version: String): WebApiBuilder = {
    webApiBuilder.withVersion(version)
    this
  }

  def withTermsOfService(terms: String): WebApiBuilder = {
    webApiBuilder.withTermsOfService(terms)
    this
  }

  def withProvider(provider: Organization): WebApiBuilder = {
    webApiBuilder.withProvider(provider.element)
    this
  }

  def withLicense(license: License): WebApiBuilder = {
    webApiBuilder.withLicense(license.element)
    this
  }

  def withDocumentation(documentation: CreativeWork): WebApiBuilder = {
    webApiBuilder.withDocumentation(documentation.element)
    this
  }

  def withBaseUriParameters(parameters: java.util.List[Parameter]): WebApiBuilder = {
    webApiBuilder.withBaseUriParameters(parameters.asScala.map(_.element).toList)
    this
  }

  def build: WebApi = WebApi(webApiBuilder.build)
}
