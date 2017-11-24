package amf.model

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.JSExportAll

import amf.plugins.domain.webapi.models.security
/**
  * JS Settings model class.
  */
@JSExportAll
class Settings private[model] (private val settings: security.Settings) extends DomainElement {
  def this() = this(security.Settings())

  val additionalProperties: DataNode = DataNode(settings.additionalProperties)

  override private[amf] def element: amf.plugins.domain.webapi.models.security.Settings = settings

  /** Set additionalProperties property of this [[Settings]]. */
  def withAdditionalProperties(additionalProperties: DataNode): this.type = {
    settings.withAdditionalProperties(additionalProperties.dataNode)
    this
  }
}

case class OAuth1Settings private[model] (private val settings: security.OAuth1Settings)
    extends Settings(settings) {
  def this() = this(security.OAuth1Settings())

  val requestTokenUri: String         = settings.requestTokenUri
  val authorizationUri: String        = settings.authorizationUri
  val tokenCredentialsUri: String     = settings.tokenCredentialsUri
  val signatures: js.Iterable[String] = settings.signatures.toJSArray

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

case class OAuth2Settings private[model] (private val settings: security.OAuth2Settings)
    extends Settings(settings) {
  def this() = this(security.OAuth2Settings())

  val authorizationUri: String                 = settings.authorizationUri
  val accessTokenUri: String                   = settings.accessTokenUri
  val authorizationGrants: js.Iterable[String] = settings.authorizationGrants.toJSArray
  val flow: String                             = settings.flow
  val scopes: js.Iterable[Scope]               = settings.scopes.map(Scope).toJSArray

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

case class ApiKeySettings private[model] (private val settings: security.ApiKeySettings)
    extends Settings(settings) {
  def this() = this(security.ApiKeySettings())

  val name: String = settings.name
  val in: String   = settings.in

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

object Settings {
  def apply(settings: amf.plugins.domain.webapi.models.security.Settings): Settings =
    (settings match {
      case oauth1: amf.plugins.domain.webapi.models.security.OAuth1Settings => Some(OAuth1Settings(oauth1))
      case oauth2: amf.plugins.domain.webapi.models.security.OAuth2Settings => Some(OAuth2Settings(oauth2))
      case apiKey: amf.plugins.domain.webapi.models.security.ApiKeySettings => Some(ApiKeySettings(apiKey))
      case s: amf.plugins.domain.webapi.models.security.Settings            => Some(Settings(s))
      case _                                          => None
    }).orNull
}
