package amf.apicontract.client.platform.model.domain.security

import amf.apicontract.client.scala.model.domain.security.{
  ApiKeySettings => InternalApiKeySettings,
  HttpApiKeySettings => InternalHttpApiKeySettings,
  HttpSettings => InternalHttpSettings,
  OAuth1Settings => InternalOAuth1Settings,
  OAuth2Settings => InternalOAuth2Settings,
  OpenIdConnectSettings => InternalOpenIdConnectSettings,
  MutualTLSSettings => InternalMutualTLSSettings,
  Settings => InternalSettings
}
import amf.apicontract.internal.convert.ApiClientConverters._
import amf.core.client.platform.model.StrField
import amf.core.client.platform.model.domain.{DataNode, DomainElement}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/** Settings model class.
  */
@JSExportAll
class Settings(override private[amf] val _internal: InternalSettings) extends DomainElement {

  @JSExportTopLevel("Settings")
  def this() = this(InternalSettings())

  def additionalProperties: DataNode = _internal.additionalProperties

  /** Set additionalProperties property of this Settings. */
  def withAdditionalProperties(properties: DataNode): this.type = {
    _internal.withAdditionalProperties(properties)
    this
  }
}

@JSExportAll
case class OAuth1Settings(override private[amf] val _internal: InternalOAuth1Settings) extends Settings(_internal) {

  @JSExportTopLevel("OAuth1Settings")
  def this() = this(InternalOAuth1Settings())

  def requestTokenUri: StrField        = _internal.requestTokenUri
  def authorizationUri: StrField       = _internal.authorizationUri
  def tokenCredentialsUri: StrField    = _internal.tokenCredentialsUri
  def signatures: ClientList[StrField] = _internal.signatures.asClient

  /** Set requestTokenUri property of this OAuth1Settings. */
  def withRequestTokenUri(requestTokenUri: String): this.type = {
    _internal.withRequestTokenUri(requestTokenUri)
    this
  }

  /** Set authorizationUri property of this OAuth1Settings] */
  def withAuthorizationUri(authorizationUri: String): this.type = {
    _internal.withAuthorizationUri(authorizationUri)
    this
  }

  /** Set tokenCredentialsUri property of this OAuth1Settings] */
  def withTokenCredentialsUri(tokenCredentialsUri: String): this.type = {
    _internal.withTokenCredentialsUri(tokenCredentialsUri)
    this
  }

  /** Set signatures property of this OAuth1Settings] */
  def withSignatures(signatures: ClientList[String]): this.type = {
    _internal.withSignatures(signatures.asInternal)
    this
  }
}

@JSExportAll
case class OAuth2Settings(override private[amf] val _internal: InternalOAuth2Settings) extends Settings(_internal) {

  @JSExportTopLevel("OAuth2Settings")
  def this() = this(InternalOAuth2Settings())

  def flows: ClientList[OAuth2Flow]             = _internal.flows.asClient
  def authorizationGrants: ClientList[StrField] = _internal.authorizationGrants.asClient

  /** Set flows property of this OAuth2Settings. */
  def withFlows(flows: ClientList[OAuth2Flow]): this.type = {
    _internal.withFlows(flows.asInternal)
    this
  }

  /** Set authorizationGrants property of this OAuth2Settings] */
  def withAuthorizationGrants(grants: ClientList[String]): this.type = {
    _internal.withAuthorizationGrants(grants.asInternal)
    this
  }
}

@JSExportAll
case class ApiKeySettings(override private[amf] val _internal: InternalApiKeySettings) extends Settings(_internal) {

  @JSExportTopLevel("ApiKeySettings")
  def this() = this(InternalApiKeySettings())

  def name: StrField            = _internal.name
  def in: StrField              = _internal.in
  def scopes: ClientList[Scope] = _internal.scopes.asClient

  /** Set authorizationUri property of this ApiKeySettings. */
  def withName(name: String): this.type = {
    _internal.withName(name)
    this
  }

  /** Set in property of this ApiKeySettings] */
  def withIn(inVal: String): this.type = {
    _internal.withIn(inVal)
    this
  }

  /** Set scopes property of this ApiKeySettings. */
  def withScopes(scopes: ClientList[Scope]): this.type = {
    _internal.withScopes(scopes.asInternal)
    this
  }
}

@JSExportAll
case class HttpApiKeySettings(override private[amf] val _internal: InternalHttpApiKeySettings)
    extends Settings(_internal) {

  @JSExportTopLevel("HttpApiKeySettings")
  def this() = this(InternalHttpApiKeySettings())

  def name: StrField = _internal.name
  def in: StrField   = _internal.in

  /** Set authorizationUri property of this HttpApiKeySettings. */
  def withName(name: String): this.type = {
    _internal.withName(name)
    this
  }

  /** Set in property of this HttpApiKeySettings] */
  def withIn(inVal: String): this.type = {
    _internal.withIn(inVal)
    this
  }
}

@JSExportAll
case class HttpSettings(override private[amf] val _internal: InternalHttpSettings) extends Settings(_internal) {

  @JSExportTopLevel("HttpSettings")
  def this() = this(InternalHttpSettings())

  def scheme: StrField          = _internal.scheme
  def bearerFormat: StrField    = _internal.bearerFormat
  def scopes: ClientList[Scope] = _internal.scopes.asClient

  /** Set scheme property of this HttpSettings. */
  def withScheme(scheme: String): this.type = {
    _internal.withScheme(scheme)
    this
  }

  /** Set bearerFormat property of this HttpSettings] */
  def withBearerFormat(bearerFormat: String): this.type = {
    _internal.withBearerFormat(bearerFormat)
    this
  }

  /** Set scopes property of this HttpSettings. */
  def withScopes(scopes: ClientList[Scope]): this.type = {
    _internal.withScopes(scopes.asInternal)
    this
  }
}

@JSExportAll
case class OpenIdConnectSettings(override private[amf] val _internal: InternalOpenIdConnectSettings)
    extends Settings(_internal) {

  @JSExportTopLevel("OpenIdConnectSettings")
  def this() = this(InternalOpenIdConnectSettings())

  def url: StrField             = _internal.url
  def scopes: ClientList[Scope] = _internal.scopes.asClient

  /** Set openIdConnectUrl property of this OpenIdConnectSettings. */
  def withUrl(url: String): this.type = {
    _internal.withUrl(url)
    this
  }

  /** Set scopes property of this OpenIdConnectSettings. */
  def withScopes(scopes: ClientList[Scope]): this.type = {
    _internal.withScopes(scopes.asInternal)
    this
  }
}

@JSExportAll
case class MutualTLSSettings(override private[amf] val _internal: InternalMutualTLSSettings)
  extends Settings(_internal) {

  @JSExportTopLevel("MutualTLSSettings")
  def this() = this(InternalMutualTLSSettings())

  def scopes: ClientList[Scope] = _internal.scopes.asClient

  /** Set scopes property of this MutualTLSSettings. */
  def withScopes(scopes: ClientList[Scope]): this.type = {
    _internal.withScopes(scopes.asInternal)
    this
  }
}
