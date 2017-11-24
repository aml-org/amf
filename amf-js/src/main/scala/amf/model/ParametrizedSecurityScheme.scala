package amf.model

import scala.scalajs.js.annotation.JSExportAll
import amf.plugins.domain.webapi.models.security

/**
  * JS ParametrizedSecurityScheme model class.
  */
@JSExportAll
case class ParametrizedSecurityScheme private[model] (private val s: security.ParametrizedSecurityScheme)
    extends DomainElement {
  def this() = this(security.ParametrizedSecurityScheme())

  val name: String       = s.name
  val scheme: String     = s.scheme
  val settings: Settings = Settings(s.settings)

  override private[amf] def element: amf.plugins.domain.webapi.models.security.ParametrizedSecurityScheme = s

  /** Set name property of this [[ParametrizedSecurityScheme]]. */
  def withName(name: String): this.type = {
    s.withName(name)
    this
  }

  def withScheme(scheme: String): this.type = {
    s.withScheme(scheme)
    this
  }

  def withSettings(settings: Settings): this.type = {
    s.withSettings(settings.element)
    this
  }

  def withDefaultSettings(): Settings = Settings(s.withDefaultSettings())

  def withOAuth1Settings(): OAuth1Settings = OAuth1Settings(s.withOAuth1Settings())

  def withOAuth2Settings(): OAuth2Settings = OAuth2Settings(s.withOAuth2Settings())

  def withApiKeySettings(): ApiKeySettings = ApiKeySettings(s.withApiKeySettings())
}
