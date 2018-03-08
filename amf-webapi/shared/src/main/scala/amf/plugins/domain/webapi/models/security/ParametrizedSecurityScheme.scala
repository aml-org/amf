package amf.plugins.domain.webapi.models.security

import amf.client.model.StrField
import amf.core.model.domain.DomainElement
import amf.core.parser.{Annotations, Fields}
import amf.plugins.domain.webapi.metamodel.security.ParametrizedSecuritySchemeModel.{Settings => SettingsField, _}
import amf.plugins.domain.webapi.metamodel.security.ParametrizedSecuritySchemeModel
import org.yaml.model.YPart

case class ParametrizedSecurityScheme(fields: Fields, annotations: Annotations)
    extends DomainElement
    with WithSettings {

  def name: StrField         = fields.field(Name)
  def scheme: SecurityScheme = fields.field(Scheme)
  def settings: Settings     = fields.field(SettingsField)

  def withName(name: String): this.type             = set(Name, name)
  def withScheme(scheme: SecurityScheme): this.type = set(Scheme, scheme)
  def withSettings(settings: Settings): this.type   = set(SettingsField, settings)

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

  override def adopted(parent: String): this.type = withId(parent + "/" + name.value())

  override def meta = ParametrizedSecuritySchemeModel
}

object ParametrizedSecurityScheme {

  def apply(): ParametrizedSecurityScheme = apply(Annotations())

  def apply(part: YPart): ParametrizedSecurityScheme = apply(Annotations(part))

  def apply(annotations: Annotations): ParametrizedSecurityScheme =
    new ParametrizedSecurityScheme(Fields(), annotations)
}
