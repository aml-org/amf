package amf.apicontract.client.scala.model.domain.security

import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.domain.NamedDomainElement
import amf.core.internal.annotations.NullSecurity
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.core.internal.utils.AmfStrings
import amf.apicontract.internal.metamodel.domain.security.ParametrizedSecuritySchemeModel
import amf.apicontract.internal.metamodel.domain.security.ParametrizedSecuritySchemeModel.{Settings => SettingsField, _}
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
