package amf.domain.security

import amf.domain.{Annotations, DomainElement, Fields}
import amf.metadata.domain.security.ParametrizedSecuritySchemeModel.{Settings => SettingsField, _}
import org.yaml.model.YPart

case class ParametrizedSecurityScheme(fields: Fields, annotations: Annotations)
    extends DomainElement
    with WithSettings {
  def name: String       = fields(Name)
  def scheme: String     = fields(Scheme)
  def settings: Settings = fields(SettingsField)

  def withName(name: String): this.type           = set(Name, name)
  def withScheme(scheme: String): this.type       = set(Scheme, scheme)
  def withSettings(settings: Settings): this.type = set(SettingsField, settings)

  def withDefaultSettings(): Settings = {
    val settings = Settings()
    set(SettingsField, settings)
    settings
  }

  def withOAuth1Settings(): OAuth1Settings = {
    val settings = OAuth1Settings()
    set(SettingsField, settings)
    settings
  }

  def withOAuth2Settings(): OAuth2Settings = {
    val settings = OAuth2Settings()
    set(SettingsField, settings)
    settings
  }

  def withApiKeySettings(): ApiKeySettings = {
    val settings = ApiKeySettings()
    set(SettingsField, settings)
    settings
  }

  override def adopted(parent: String): this.type = withId(parent + "/" + name)
}

object ParametrizedSecurityScheme {

  def apply(): ParametrizedSecurityScheme = apply(Annotations())

  def apply(part: YPart): ParametrizedSecurityScheme = apply(Annotations(part))

  def apply(annotations: Annotations): ParametrizedSecurityScheme =
    new ParametrizedSecurityScheme(Fields(), annotations)
}
