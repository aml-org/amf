package amf.apicontract.client.platform.model.domain.api

import amf.apicontract.client.platform.model.domain._
import amf.apicontract.client.platform.model.domain.security.SecurityRequirement
import amf.apicontract.client.scala.model.domain.api.{Api => InternalApi}
import amf.apicontract.internal.convert.ApiClientConverters._
import amf.core.client.platform.model.StrField
import amf.core.client.platform.model.domain.{DomainElement, NamedDomainElement}
import amf.core.internal.remote.SpecId
import amf.shapes.client.platform.model.domain.CreativeWork

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

  def sourceVendor: ClientOption[SpecId] = _internal.sourceVendor.asClient

}

trait ApiFieldSetter[T] {

  private[amf] val _internal: InternalApi

  def withDescription(description: String): T
  def withIdentifier(identifier: String): T
  def withSchemes(schemes: ClientList[String]): T
  def withEndPoints(endPoints: ClientList[EndPoint]): T
  def withAccepts(accepts: ClientList[String]): T
  def withContentType(contentType: ClientList[String]): T
  def withVersion(version: String): T
  def withTermsOfService(terms: String): T
  def withProvider(provider: Organization): T
  def withLicense(license: License): T
  def withDocumentation(documentations: ClientList[CreativeWork]): T
  def withServers(servers: ClientList[Server]): T
  def withSecurity(security: ClientList[SecurityRequirement]): T

}
