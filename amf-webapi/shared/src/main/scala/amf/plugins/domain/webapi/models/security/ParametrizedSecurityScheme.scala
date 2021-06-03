package amf.plugins.domain.webapi.models.security

import amf.core.annotations.NullSecurity
import amf.core.metamodel.Field
import amf.core.model.StrField
import amf.core.model.domain.{DomainElement, NamedDomainElement}
import amf.core.parser.{Annotations, Fields}
import amf.core.utils.AmfStrings
import amf.plugins.domain.webapi.metamodel.security.ParametrizedSecuritySchemeModel
import amf.plugins.domain.webapi.metamodel.security.ParametrizedSecuritySchemeModel.{Settings => SettingsField, _}
import org.yaml.model.YPart

case class ParametrizedSecurityScheme(fields: Fields, annotations: Annotations)
    extends NamedDomainElement
    with WithSettings {

  override protected def nameField: Field = Name
  def description: StrField               = fields.field(Description)
  def scheme: SecurityScheme              = fields.field(Scheme)
  def settings: Settings                  = fields.field(SettingsField)

  def withDescription(descr: String): this.type     = set(Description, descr)
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

  def withHttpApiKeySettings(): HttpApiKeySettings = {
    val settings = HttpApiKeySettings()
    set(SettingsField, settings)
    settings
  }

  def withHttpSettings(): HttpSettings = {
    val settings = HttpSettings()
    set(SettingsField, settings)
    settings
  }

  def withOpenIdConnectSettings(): OpenIdConnectSettings = {
    val settings = OpenIdConnectSettings()
    set(SettingsField, settings)
    settings
  }

  def hasNullSecurityScheme: Boolean = annotations.find(classOf[NullSecurity]).nonEmpty

  override def meta: ParametrizedSecuritySchemeModel.type = ParametrizedSecuritySchemeModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = "/" + name.option().getOrElse("default-parametrized").urlComponentEncoded
}

object ParametrizedSecurityScheme {

  def apply(): ParametrizedSecurityScheme = apply(Annotations())

  def apply(part: YPart): ParametrizedSecurityScheme = apply(Annotations(part))

  def apply(annotations: Annotations): ParametrizedSecurityScheme =
    new ParametrizedSecurityScheme(Fields(), annotations)
}
