package amf.client.model.domain

import amf.client.convert.WebApiClientConverters._
import amf.client.model.StrField
import amf.core.remote.Vendor
import amf.plugins.domain.webapi.models.{WebApi => InternalWebApi}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * WebApi model class.
  */
@JSExportAll
case class WebApi(override private[amf] val _internal: InternalWebApi) extends DomainElement with NamedDomainElement {

  @JSExportTopLevel("model.domain.WebApi")
  def this() = this(InternalWebApi())

  def name: StrField                            = _internal.name
  def description: StrField                     = _internal.description
  def schemes: ClientList[StrField]             = _internal.schemes.asClient
  def endPoints: ClientList[EndPoint]           = _internal.endPoints.asClient
  def accepts: ClientList[StrField]             = _internal.accepts.asClient
  def contentType: ClientList[StrField]         = _internal.contentType.asClient
  def version: StrField                         = _internal.version
  def termsOfService: StrField                  = _internal.termsOfService
  def provider: Organization                    = _internal.provider
  def license: License                          = _internal.license
  def documentations: ClientList[CreativeWork]  = _internal.documentations.asClient
  def servers: ClientList[Server]               = _internal.servers.asClient
  def security: ClientList[SecurityRequirement] = _internal.security.asClient

  /** Set name property of this WebApi. */
  def withName(name: String): this.type = {
    _internal.withName(name)
    this
  }

  /** Set description property of this WebApi. */
  def withDescription(description: String): this.type = {
    _internal.withDescription(description)
    this
  }

  /** Set schemes property of this WebApi. */
  def withSchemes(schemes: ClientList[String]): this.type = {
    _internal.withSchemes(schemes.asInternal)
    this
  }

  /** Set endPoints property of this WebApi. */
  def withEndPoints(endPoints: ClientList[EndPoint]): this.type = {
    _internal.withEndPoints(endPoints.asInternal)
    this
  }

  /** Set accepts property of this WebApi. */
  def withAccepts(accepts: ClientList[String]): this.type = {
    _internal.withAccepts(accepts.asInternal)
    this
  }

  /** Set contentType property of this WebApi. */
  def withContentType(contentType: ClientList[String]): this.type = {
    _internal.withContentType(contentType.asInternal)
    this
  }

  /** Set version property of this WebApi. */
  def withVersion(version: String): this.type = {
    _internal.withVersion(version)
    this
  }

  /** Set termsOfService property of this WebApi. */
  def withTermsOfService(terms: String): this.type = {
    _internal.withTermsOfService(terms)
    this
  }

  /** Set provider property of this WebApi using a Organization */
  def withProvider(provider: Organization): this.type = {
    _internal.withProvider(provider)
    this
  }

  /** Set license property of this WebApi using a License */
  def withLicense(license: License): this.type = {
    _internal.withLicense(license)
    this
  }

  /** Set documentation property of this WebApi using a CreativeWork */
  def withDocumentation(documentations: ClientList[CreativeWork]): this.type = {
    _internal.withDocumentations(documentations.asInternal)
    this
  }

  /** Set servers property of this WebApi using a list of Server objects. */
  def withServers(servers: ClientList[Server]): this.type = {
    _internal.withServers(servers.asInternal)
    this
  }

  /** Set security property of this WebApi using a list of SecurityRequirement */
  def withSecurity(security: ClientList[SecurityRequirement]): this.type = {
    _internal.withSecurity(security.asInternal)
    this
  }

  /**
    * Adds one CreativeWork to the documentations property of this WebApi and returns it for population.
    * Path property of the CreativeWork is required.
    */
  def withDocumentationTitle(title: String): CreativeWork = _internal.withDocumentationTitle(title)

  /**
    * Adds one CreativeWork to the documentations property of this WebApi and returns it for population.
    * Path property of the CreativeWork is required.
    */
  def withDocumentationUrl(url: String): CreativeWork = _internal.withDocumentationUrl(url)

  /**
    * Adds one EndPoint to the endPoints property of this WebApi and returns it for population.
    * Path property of the endPoint is required.
    */
  def withEndPoint(path: String): EndPoint = _internal.withEndPoint(path)

  /**
    * Adds one Server to the servers property of this WebApi and returns it for population.
    * url property of the server is required.
    */
  def withServer(url: String): Server = _internal.withServer(url)

  /**
    * Adds one Server to the servers property of this WebApi and returns it for population.
    * url property of the server is required.
    * This method marks this Server as default. When generating this Server in RAML or OAS2, it will generate a
    * 'baseUri', 'host' or 'basePath', instead of a 'servers' annotation.
    */
  def withDefaultServer(url: String): Server = _internal.withDefaultServer(url)

  def sourceVendor: Option[Vendor] = _internal.sourceVendor
}
