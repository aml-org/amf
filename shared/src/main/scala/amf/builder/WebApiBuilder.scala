package amf.builder

import amf.model.{CreativeWork, License, Organization, WebApi}

/**
  * Created by martin.gutierrez on 6/29/17.
  */
class WebApiBuilder extends Builder[WebApi] {
  var name: String                = _
  var description: String         = _
  var host: String                = _
  var scheme: List[String]        = _
  var basePath: String            = _
  var accepts: String             = _
  var contentType: String         = _
  var version: String             = _
  var termsOfService: String      = _
  var provider: Organization      = _
  var license: License            = _
  var documentation: CreativeWork = _

  def withName(name: String): this.type = {
    this.name = name
    this
  }

  def withDescription(description: String): this.type = {
    this.description = description
    this
  }

  def withHost(host: String): this.type = {
    this.host = host
    this
  }

  def withScheme(scheme: List[String]): this.type = {
    this.scheme = scheme
    this
  }

  def withBasePath(basePath: String): this.type = {
    this.basePath = basePath
    this
  }

  def withAccepts(accepts: String): this.type = {
    this.accepts = accepts
    this
  }

  def withContentType(contentType: String): this.type = {
    this.contentType = contentType
    this
  }

  def withVersion(version: String): this.type = {
    this.version = version
    this
  }

  def withTermsOfService(termsOfService: String): this.type = {
    this.termsOfService = termsOfService
    this
  }

  def withProvider(provider: Organization): this.type = {
    this.provider = provider
    this
  }

  def withProvider(url: String, name: String, email: String): this.type = {
    provider = new Organization(url, name, email)
    this
  }

  def withLicense(license: License): this.type = {
    this.license = license
    this
  }

  def withLicense(url: String, name: String): this.type = {
    license = new License(url, name)
    this
  }

  def withDocumentation(documentation: CreativeWork): this.type = {
    this.documentation = documentation
    this
  }

  def withDocumentation(url: String, description: String): this.type = {
    documentation = new CreativeWork(url, description)
    this
  }

  override def build: WebApi =
    new WebApi(name,
               description,
               host,
               scheme,
               basePath,
               accepts,
               contentType,
               version,
               termsOfService,
               provider,
               license,
               documentation)
}

object WebApiBuilder {
  def apply(): WebApiBuilder = new WebApiBuilder()
}
