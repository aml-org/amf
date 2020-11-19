package amf.client.model.domain

import amf.client.convert.WebApiClientConverters._
import amf.client.model.StrField
import amf.core.remote.Vendor
import amf.plugins.domain.webapi.models.api.{Api => InternalApi}

import scala.scalajs.js.annotation.JSExportAll

/**
  * Api model class.
  */
@JSExportAll
abstract class Api[A](override private[amf] val _internal: InternalApi)
    extends ApiFieldSetter[A]
    with DomainElement
    with NamedDomainElement {

  def name: StrField                            = _internal.name
  def description: StrField                     = _internal.description
  def identifier: StrField                      = _internal.identifier
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

trait ApiFieldSetter[T] {

  private[amf] val _internal: InternalApi

  /** Set description property of this WebApi. */
  def withDescription(description: String): T = {
    _internal.withDescription(description)
    this.asInstanceOf[T]
  }

  /** Set identifier property of this WebApi. */
  def withIdentifier(identifier: String): T = {
    _internal.withIdentifier(identifier)
    this.asInstanceOf[T]
  }

  /** Set schemes property of this WebApi. */
  def withSchemes(schemes: ClientList[String]): T = {
    _internal.withSchemes(schemes.asInternal)
    this.asInstanceOf[T]
  }

  /** Set endPoints property of this WebApi. */
  def withEndPoints(endPoints: ClientList[EndPoint]): T = {
    _internal.withEndPoints(endPoints.asInternal)
    this.asInstanceOf[T]
  }

  /** Set accepts property of this WebApi. */
  def withAccepts(accepts: ClientList[String]): T = {
    _internal.withAccepts(accepts.asInternal)
    this.asInstanceOf[T]
  }

  /** Set contentType property of this WebApi. */
  def withContentType(contentType: ClientList[String]): T = {
    _internal.withContentType(contentType.asInternal)
    this.asInstanceOf[T]
  }

  /** Set version property of this WebApi. */
  def withVersion(version: String): T = {
    _internal.withVersion(version)
    this.asInstanceOf[T]
  }

  /** Set termsOfService property of this WebApi. */
  def withTermsOfService(terms: String): T = {
    _internal.withTermsOfService(terms)
    this.asInstanceOf[T]
  }

  /** Set provider property of this WebApi using a Organization */
  def withProvider(provider: Organization): T = {
    _internal.withProvider(provider)
    this.asInstanceOf[T]
  }

  /** Set license property of this WebApi using a License */
  def withLicense(license: License): T = {
    _internal.withLicense(license)
    this.asInstanceOf[T]
  }

  /** Set documentation property of this WebApi using a CreativeWork */
  def withDocumentation(documentations: ClientList[CreativeWork]): T = {
    _internal.withDocumentations(documentations.asInternal)
    this.asInstanceOf[T]
  }

  /** Set servers property of this WebApi using a list of Server objects. */
  def withServers(servers: ClientList[Server]): T = {
    _internal.withServers(servers.asInternal)
    this.asInstanceOf[T]
  }

  /** Set security property of this WebApi using a list of SecurityRequirement */
  def withSecurity(security: ClientList[SecurityRequirement]): T = {
    _internal.withSecurity(security.asInternal)
    this.asInstanceOf[T]
  }

}
