package amf.model

import amf.model.domain.{ApiKeySettings, OAuth1Settings, OAuth2Settings, Settings}

import scala.collection.JavaConverters._
import amf.plugins.domain.webapi.models.security

/**
  * JS Settings model class.
  */
class Settings private[model] (private val settings: security.Settings) extends DomainElement {
  def this() = this(security.Settings())

  val additionalProperties: DataNode = DataNode(settings.additionalProperties)

  override private[amf] def element: amf.plugins.domain.webapi.models.security.Settings = settings

  /** Set additionalProperties property of this [[domain.Settings]]. */
  def withAdditionalProperties(additionalProperties: DataNode): this.type = {
    settings.withAdditionalProperties(additionalProperties.dataNode)
    this
  }
}

case class OAuth1Settings private[model] (private val settings: amf.plugins.domain.webapi.models.security.OAuth1Settings)
    extends domain.Settings(settings) {
  def this() = this(security.OAuth1Settings())

  val requestTokenUri: String            = settings.requestTokenUri
  val authorizationUri: String           = settings.authorizationUri
  val tokenCredentialsUri: String        = settings.tokenCredentialsUri
  val signatures: java.util.List[String] = settings.signatures.asJava

  /** Set requestTokenUri property of this [[domain.OAuth1Settings]]. */
  def withRequestTokenUri(requestTokenUri: String): this.type = {
    settings.withRequestTokenUri(requestTokenUri)
    this

  }

  /** Set authorizationUri property of this [[domain.OAuth1Settings]]. */
  def withAuthorizationUri(authorizationUri: String): this.type = {
    settings.withAuthorizationUri(authorizationUri)
    this
  }

  /** Set tokenCredentialsUri property of this [[domain.OAuth1Settings]]. */
  def withTokenCredentialsUri(tokenCredentialsUri: String): this.type = {
    settings.withTokenCredentialsUri(tokenCredentialsUri)
    this
  }

  /** Set signatures property of this [[domain.OAuth1Settings]]. */
  def withSignatures(signatures: java.util.List[String]): this.type = {
    settings.withSignatures(signatures.asScala)
    this
  }

  override private[amf] def element: amf.plugins.domain.webapi.models.security.OAuth1Settings = settings
}

case class OAuth2Settings private[model] (private val settings: amf.plugins.domain.webapi.models.security.OAuth2Settings)
    extends domain.Settings(settings) {
  def this() = this(security.OAuth2Settings())

  val authorizationUri: String                    = settings.authorizationUri
  val accessTokenUri: String                      = settings.accessTokenUri
  val authorizationGrants: java.util.List[String] = settings.authorizationGrants.asJava
  val flow: String                                = settings.flow
  val scopes: java.util.List[Scope]               = settings.scopes.map(Scope).asJava

  /** Set authorizationUri property of this [[domain.OAuth2Settings]]. */
  def withAuthorizationUri(authorizationUri: String): this.type = {
    settings.withAuthorizationUri(authorizationUri)
    this
  }

  /** Set accessTokenUri property of this [[domain.OAuth2Settings]]. */
  def withAccessTokenUri(accessTokenUri: String): this.type = {
    settings.withAccessTokenUri(accessTokenUri)
    this
  }

  /** Set authorizationGrants property of this [[domain.OAuth2Settings]]. */
  def withAuthorizationGrants(authorizationGrants: java.util.List[String]): this.type = {
    settings.withAuthorizationGrants(authorizationGrants.asScala)
    this
  }

  /** Set flow property of this [[domain.OAuth2Settings]]. */
  def withFlow(flow: String): this.type = {
    settings.withFlow(flow)
    this
  }

  /** Set scopes property of this [[domain.OAuth2Settings]]. */
  def withScopes(scopes: java.util.List[Scope]): this.type = {
    settings.withScopes(scopes.asScala.map(_.element))
    this
  }

  override private[amf] def element: amf.plugins.domain.webapi.models.security.OAuth2Settings = settings
}

case class ApiKeySettings private[model] (private val settings: amf.plugins.domain.webapi.models.security.ApiKeySettings)
    extends domain.Settings(settings) {
  def this() = this(security.ApiKeySettings())

  val name: String = settings.name
  val in: String   = settings.in

  /** Set authorizationUri property of this [[domain.ApiKeySettings]]. */
  def withName(name: String): this.type = {
    settings.withName(name)
    this
  }

  /** Set in property of this [[domain.ApiKeySettings]]. */
  def withIn(in: String): this.type = {
    settings.withIn(in)
    this
  }

  override private[amf] def element: amf.plugins.domain.webapi.models.security.ApiKeySettings = settings
}

object Settings {
  def apply(settings: amf.plugins.domain.webapi.models.security.Settings): domain.Settings =
    (settings match {
      case oauth1: amf.plugins.domain.webapi.models.security.OAuth1Settings => Some(OAuth1Settings(oauth1))
      case oauth2: amf.plugins.domain.webapi.models.security.OAuth2Settings => Some(OAuth2Settings(oauth2))
      case apiKey: amf.plugins.domain.webapi.models.security.ApiKeySettings => Some(ApiKeySettings(apiKey))
      case s: amf.plugins.domain.webapi.models.security.Settings            => Some(Settings(s))
      case _                                          => None
    }).orNull
}
