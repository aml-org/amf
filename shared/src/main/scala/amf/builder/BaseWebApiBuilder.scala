package amf.builder

import amf.model._

import scala.scalajs.js.annotation.JSExportAll

/**
  * Created by martin.gutierrez on 7/3/17.
  */
@JSExportAll
trait BaseWebApiBuilder extends Builder[WebApiModel] {

  protected var name: String                = _
  protected var description: String         = _
  protected var host: String                = _
  protected var scheme: List[String]        = _
  protected var basePath: String            = _
  protected var accepts: String             = _
  protected var contentType: String         = _
  protected var version: String             = _
  protected var termsOfService: String      = _
  protected var provider: Organization      = _
  protected var license: License            = _
  protected var documentation: CreativeWork = _

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
}
