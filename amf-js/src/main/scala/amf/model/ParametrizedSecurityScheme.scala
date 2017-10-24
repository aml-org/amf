package amf.model

import scala.scalajs.js.annotation.JSExportAll

/**
  * JS ParametrizedSecurityScheme model class.
  */
@JSExportAll
case class ParametrizedSecurityScheme private[model] (private val s: amf.domain.security.ParametrizedSecurityScheme)
    extends DomainElement {
  def this() = this(amf.domain.security.ParametrizedSecurityScheme())

  val name: String       = s.name
  val scheme: String     = s.scheme
  val settings: Settings = Settings(s.settings)

  override private[amf] def element: amf.domain.security.ParametrizedSecurityScheme = s

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
