package amf.client.model.domain

import amf.client.convert.WebApiClientConverters._
import amf.client.model.StrField
import amf.plugins.domain.webapi.models.security.{
  OpenIdConnectSettings => InternalOpenIdConnectSettings,
  HttpSettings => InternalHttpSettings,
  ApiKeySettings => InternalApiKeySettings,
  OAuth1Settings => InternalOAuth1Settings,
  OAuth2Settings => InternalOAuth2Settings,
  Settings => InternalSettings
}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * Settings model class.
  */
@JSExportAll
class Settings(override private[amf] val _internal: InternalSettings) extends DomainElement {

  @JSExportTopLevel("model.domain.Settings")
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

  @JSExportTopLevel("model.domain.OAuth1Settings")
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

  @JSExportTopLevel("model.domain.OAuth2Settings")
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

  @JSExportTopLevel("model.domain.ApiKeySettings")
  def this() = this(InternalApiKeySettings())

  def name: StrField = _internal.name
  def in: StrField   = _internal.in

  /** Set authorizationUri property of this ApiKeySettings. */
  def withName(name: String): this.type = {
    _internal.withName(name)
    this
  }

  /** Set in property of this ApiKeySettings] */
  def withIn(in: String): this.type = {
    _internal.withIn(in)
    this
  }
}

@JSExportAll
case class HttpSettings(override private[amf] val _internal: InternalHttpSettings) extends Settings(_internal) {

  @JSExportTopLevel("model.domain.HttpSettings")
  def this() = this(InternalHttpSettings())

  def scheme: StrField       = _internal.scheme
  def bearerFormat: StrField = _internal.bearerFormat

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
}

@JSExportAll
case class OpenIdConnectSettings(override private[amf] val _internal: InternalOpenIdConnectSettings)
    extends Settings(_internal) {

  @JSExportTopLevel("model.domain.OpenIdConnectSettings")
  def this() = this(InternalOpenIdConnectSettings())

  def url: StrField = _internal.url

  /** Set openIdConnectUrl property of this OpenIdConnectSettings. */
  def withUrl(url: String): this.type = {
    _internal.withUrl(url)
    this
  }
}
