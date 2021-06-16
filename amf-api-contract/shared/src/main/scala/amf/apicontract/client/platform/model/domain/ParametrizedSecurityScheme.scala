package amf.apicontract.client.platform.model.domain

import amf.core.client.platform.model.StrField
import amf.core.client.platform.model.domain.DomainElement

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * ParametrizedSecurityScheme model class.
  */
@JSExportAll
case class ParametrizedSecurityScheme(override private[amf] val _internal: InternalParametrizedSecurityScheme)
    extends DomainElement {

  @JSExportTopLevel("model.domain.ParametrizedSecurityScheme")
  def this() = this(InternalParametrizedSecurityScheme())

  def name: StrField         = _internal.name
  def description: StrField  = _internal.description
  def scheme: SecurityScheme = _internal.scheme
  def settings: Settings     = _internal.settings

  /** Set name property of this ParametrizedSecurityScheme. */
  def withName(name: String): this.type = {
    _internal.withName(name)
    this
  }

  def withDescription(description: String): this.type = {
    _internal.withDescription(description)
    this
  }

  def withScheme(scheme: SecurityScheme): this.type = {
    _internal.withScheme(scheme)
    this
  }

  def withSettings(settings: Settings): this.type = {
    _internal.withSettings(settings)
    this
  }

  def withDefaultSettings(): Settings = _internal.withDefaultSettings()

  def withOAuth1Settings(): OAuth1Settings = _internal.withOAuth1Settings()

  def withOAuth2Settings(): OAuth2Settings = _internal.withOAuth2Settings()

  def withApiKeySettings(): ApiKeySettings = _internal.withApiKeySettings()

  def withHttpSettings(): HttpSettings = _internal.withHttpSettings()

  def withOpenIdConnectSettings(): OpenIdConnectSettings = _internal.withOpenIdConnectSettings()

  // If the security scheme is null and not "null" it returns true
  def hasNullSecurityScheme: Boolean = _internal.hasNullSecurityScheme
}
