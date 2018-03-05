package amf.model.domain

import amf.plugins.domain.webapi.models.security

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * JS Settings model class.
  */
@JSExportAll
class Settings(private[model] val settings: security.Settings) extends DomainElement {

  @JSExportTopLevel("model.domain.Settings")
  def this() = this(security.Settings())

  def additionalProperties: DataNode = DataNode(settings.additionalProperties)

  override private[amf] def element: amf.plugins.domain.webapi.models.security.Settings = settings

  /** Set additionalProperties property of this [[Settings]]. */
  def withAdditionalProperties(additionalProperties: DataNode): this.type = {
    settings.withAdditionalProperties(additionalProperties.dataNode)
    this
  }
}

@JSExportAll
case class OAuth1Settings(override private[model] val settings: security.OAuth1Settings) extends Settings(settings) {

  @JSExportTopLevel("model.domain.OAuth1Settings")
  def this() = this(security.OAuth1Settings())

  def requestTokenUri: String         = settings.requestTokenUri
  def authorizationUri: String        = settings.authorizationUri
  def tokenCredentialsUri: String     = settings.tokenCredentialsUri
  def signatures: js.Iterable[String] = Option(settings.signatures).getOrElse(Nil).toJSArray

  /** Set requestTokenUri property of this [[OAuth1Settings]]. */
  def withRequestTokenUri(requestTokenUri: String): this.type = {
    settings.withRequestTokenUri(requestTokenUri)
    this

  }

  /** Set authorizationUri property of this [[OAuth1Settings]]. */
  def withAuthorizationUri(authorizationUri: String): this.type = {
    settings.withAuthorizationUri(authorizationUri)
    this
  }

  /** Set tokenCredentialsUri property of this [[OAuth1Settings]]. */
  def withTokenCredentialsUri(tokenCredentialsUri: String): this.type = {
    settings.withTokenCredentialsUri(tokenCredentialsUri)
    this
  }

  /** Set signatures property of this [[OAuth1Settings]]. */
  def withSignatures(signatures: js.Iterable[String]): this.type = {
    settings.withSignatures(signatures.toSeq)
    this
  }

  override private[amf] def element: security.OAuth1Settings = settings
}

@JSExportAll
case class OAuth2Settings(override private[model] val settings: security.OAuth2Settings) extends Settings(settings) {

  @JSExportTopLevel("model.domain.OAuth2Settings")
  def this() = this(security.OAuth2Settings())

  def authorizationUri: String                 = settings.authorizationUri
  def accessTokenUri: String                   = settings.accessTokenUri
  def authorizationGrants: js.Iterable[String] = Option(settings.authorizationGrants).getOrElse(Nil).toJSArray
  def flow: String                             = settings.flow
  def scopes: js.Iterable[Scope]               = Option(settings.scopes).getOrElse(Nil).map(Scope).toJSArray

  /** Set authorizationUri property of this [[OAuth2Settings]]. */
  def withAuthorizationUri(authorizationUri: String): this.type = {
    settings.withAuthorizationUri(authorizationUri)
    this
  }

  /** Set accessTokenUri property of this [[OAuth2Settings]]. */
  def withAccessTokenUri(accessTokenUri: String): this.type = {
    settings.withAccessTokenUri(accessTokenUri)
    this
  }

  /** Set authorizationGrants property of this [[OAuth2Settings]]. */
  def withAuthorizationGrants(authorizationGrants: js.Iterable[String]): this.type = {
    settings.withAuthorizationGrants(authorizationGrants.toSeq)
    this
  }

  /** Set flow property of this [[OAuth2Settings]]. */
  def withFlow(flow: String): this.type = {
    settings.withFlow(flow)
    this
  }

  /** Set scopes property of this [[OAuth2Settings]]. */
  def withScopes(scopes: js.Iterable[Scope]): this.type = {
    settings.withScopes(scopes.toSeq.map(_.element))
    this
  }

  override private[amf] def element: amf.plugins.domain.webapi.models.security.OAuth2Settings = settings
}

@JSExportAll
case class ApiKeySettings(override private[model] val settings: security.ApiKeySettings) extends Settings(settings) {

  @JSExportTopLevel("model.domain.ApiKeySettings")
  def this() = this(security.ApiKeySettings())

  def name: String = settings.name
  def in: String   = settings.in

  /** Set authorizationUri property of this [[ApiKeySettings]]. */
  def withName(name: String): this.type = {
    settings.withName(name)
    this
  }

  /** Set in property of this [[ApiKeySettings]]. */
  def withIn(in: String): this.type = {
    settings.withIn(in)
    this
  }
  override private[amf] def element: amf.plugins.domain.webapi.models.security.ApiKeySettings = settings
}

@JSExportAll
object Settings {
  def apply(settings: amf.plugins.domain.webapi.models.security.Settings): Settings =
    (settings match {
      case oauth1: amf.plugins.domain.webapi.models.security.OAuth1Settings => Some(OAuth1Settings(oauth1))
      case oauth2: amf.plugins.domain.webapi.models.security.OAuth2Settings => Some(OAuth2Settings(oauth2))
      case apiKey: amf.plugins.domain.webapi.models.security.ApiKeySettings => Some(ApiKeySettings(apiKey))
      case s: amf.plugins.domain.webapi.models.security.Settings            => Some(Settings(s))
      case _                                                                => None
    }).orNull
}
