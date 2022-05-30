package amf.apicontract.client.platform.model.domain.security

import amf.core.client.platform.model.StrField
import amf.core.client.platform.model.domain.DomainElement
import amf.apicontract.client.scala.model.domain.security.{OAuth2Flow => InternalOAuth2Flow}
import amf.apicontract.internal.convert.ApiClientConverters._

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/** Scope model class.
  */
@JSExportAll
case class OAuth2Flow(override private[amf] val _internal: InternalOAuth2Flow) extends DomainElement {

  @JSExportTopLevel("OAuth2Flow")
  def this() = this(InternalOAuth2Flow())

  def authorizationUri: StrField = _internal.authorizationUri
  def accessTokenUri: StrField   = _internal.accessTokenUri
  def refreshUri: StrField       = _internal.refreshUri
  def scopes: ClientList[Scope]  = _internal.scopes.asClient
  def flow: StrField             = _internal.flow

  /** Set authorizationUri property of this Scope. */
  def withAuthorizationUri(authorizationUri: String): this.type = {
    _internal.withAuthorizationUri(authorizationUri)
    this
  }

  /** Set accessTokenUri property of this Scope. */
  def withAccessTokenUri(accessTokenUri: String): this.type = {
    _internal.withAccessTokenUri(accessTokenUri)
    this
  }

  /** Set refreshUri property of this Scope. */
  def withRefreshUri(refreshUri: String): this.type = {
    _internal.withRefreshUri(refreshUri)
    this
  }

  /** Set scopes property of this Scope. */
  def withScopes(scopes: ClientList[Scope]): this.type = {
    _internal.withScopes(scopes.asInternal)
    this
  }

  /** Set flow property of this Scope. */
  def withFlow(flow: String): this.type = {
    _internal.withFlow(flow)
    this
  }
}
