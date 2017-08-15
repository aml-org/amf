package amf.model.builder

import amf.domain._
import amf.model.{CreativeWork, License, Organization, WebApi}

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportAll

/**
  * Builder class for web api js
  */
@JSExportAll
case class WebApiBuilder(private val webApiBuilder: amf.builder.WebApiBuilder = amf.builder.WebApiBuilder())
    extends Builder {

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

  def withSchemes(schemes: js.Iterable[String]): WebApiBuilder = {
    webApiBuilder.withSchemes(schemes.toList)
    this
  }

  def withEndPoints(endPoints: js.Iterable[EndPoint]): WebApiBuilder = {
    webApiBuilder.withEndPoints(endPoints.toList)
    this
  }

  def withBasePath(path: String): WebApiBuilder = {
    webApiBuilder.withBasePath(path)
    this
  }

  def withAccepts(accepts: js.Iterable[String]): WebApiBuilder = {
    webApiBuilder.withAccepts(accepts.toList)
    this
  }

  def withContentType(contentType: js.Iterable[String]): WebApiBuilder = {
    webApiBuilder.withContentType(contentType.toList)
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

  def withBaseUriParameters(parameters: js.Iterable[Parameter]): WebApiBuilder = {
    webApiBuilder.withBaseUriParameters(parameters.toList)
    this
  }

  def build: WebApi = WebApi(webApiBuilder.build)
}
