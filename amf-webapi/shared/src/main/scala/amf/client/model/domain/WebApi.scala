package amf.client.model.domain

import amf.client.convert.WebApiClientConverters.ClientList
import amf.client.convert.WebApiClientConverters._

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}
import amf.plugins.domain.webapi.models.api.{WebApi => InternalWebApi}

@JSExportAll
case class WebApi(override private[amf] val _internal: InternalWebApi) extends Api(_internal) {

  @JSExportTopLevel("model.domain.WebApi")
  def this() = this(InternalWebApi())

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

  /** Set identifier property of this WebApi. */
  def withIdentifier(identifier: String): this.type = {
    _internal.withIdentifier(identifier)
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
}
