package amf.apicontract.client.platform.model.domain.api

import amf.apicontract.client.platform.model.domain.security.SecurityRequirement
import amf.apicontract.client.platform.model.domain.{EndPoint, License, Organization, Server, Tag}
import amf.apicontract.client.scala.model.domain.api.{WebApi => InternalWebApi}
import amf.apicontract.internal.convert.ApiClientConverters._
import amf.shapes.client.platform.model.domain.CreativeWork

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class WebApi(override private[amf] val _internal: InternalWebApi) extends Api[WebApi](_internal) {

  @JSExportTopLevel("WebApi")
  def this() = this(InternalWebApi())

  /** Set name property of this WebApi. */
  def withName(name: String): this.type = {
    _internal.withName(name)
    this
  }

  override def withDescription(description: String): this.type = {
    _internal.withDescription(description)
    this
  }

  /** Set identifier property of this WebApi. */
  override def withIdentifier(identifier: String): this.type = {
    _internal.withIdentifier(identifier)
    this
  }

  /** Set schemes property of this WebApi. */
  override def withSchemes(schemes: ClientList[String]): this.type = {
    _internal.withSchemes(schemes.asInternal)
    this
  }

  /** Set endPoints property of this WebApi. */
  override def withEndPoints(endPoints: ClientList[EndPoint]): this.type = {
    _internal.withEndPoints(endPoints.asInternal)
    this
  }

  /** Set accepts property of this WebApi. */
  override def withAccepts(accepts: ClientList[String]): this.type = {
    _internal.withAccepts(accepts.asInternal)
    this
  }

  /** Set contentType property of this WebApi. */
  override def withContentType(contentType: ClientList[String]): this.type = {
    _internal.withContentType(contentType.asInternal)
    this
  }

  /** Set version property of this WebApi. */
  override def withVersion(version: String): this.type = {
    _internal.withVersion(version)
    this
  }

  /** Set termsOfService property of this WebApi. */
  override def withTermsOfService(terms: String): this.type = {
    _internal.withTermsOfService(terms)
    this
  }

  /** Set provider property of this WebApi using a Organization */
  override def withProvider(provider: Organization): this.type = {
    _internal.withProvider(provider)
    this
  }

  /** Set license property of this WebApi using a License */
  override def withLicense(license: License): this.type = {
    _internal.withLicense(license)
    this
  }

  /** Set documentation property of this WebApi using a CreativeWork */
  override def withDocumentation(documentations: ClientList[CreativeWork]): this.type = {
    _internal.withDocumentations(documentations.asInternal)
    this
  }

  /** Set servers property of this WebApi using a list of Server objects. */
  override def withServers(servers: ClientList[Server]): this.type = {
    _internal.withServers(servers.asInternal)
    this
  }

  /** Set security property of this WebApi using a list of SecurityRequirement */
  override def withSecurity(security: ClientList[SecurityRequirement]): this.type = {
    _internal.withSecurity(security.asInternal)
    this
  }

  override def withTags(tags: ClientList[Tag]): this.type = {
    _internal.withTags(tags.asInternal)
    this
  }
}
