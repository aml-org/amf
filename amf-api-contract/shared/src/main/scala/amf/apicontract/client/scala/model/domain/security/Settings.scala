package amf.apicontract.client.scala.model.domain.security

import amf.core.client.scala.model.domain.{AmfArray, DataNode, DomainElement}
import amf.core.client.scala.model.{StrField, domain}
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.apicontract.internal.metamodel.domain.security.ApiKeySettingsModel._
import amf.apicontract.internal.metamodel.domain.security.HttpSettingsModel._
import amf.apicontract.internal.metamodel.domain.security.OAuth1SettingsModel.{AuthorizationUri => AuthorizationUri1, _}
import amf.apicontract.internal.metamodel.domain.security.OAuth2SettingsModel._
import amf.apicontract.internal.metamodel.domain.security.OpenIdConnectSettingsModel._
import amf.apicontract.internal.metamodel.domain.security.SettingsModel._
import amf.apicontract.internal.metamodel.domain.security._

class Settings(val fields: Fields, val annotations: Annotations) extends DomainElement {
  def additionalProperties: DataNode = fields(AdditionalProperties)

  def withAdditionalProperties(additionalProperties: DataNode): this.type =
    set(AdditionalProperties, additionalProperties)

  def cloneSettings(parent: String): Settings = {
    val cloned = this match {
      case _: OAuth1Settings        => OAuth1Settings(annotations)
      case _: OAuth2Settings        => OAuth2Settings(annotations)
      case _: ApiKeySettings        => ApiKeySettings(annotations)
      case _: HttpSettings          => HttpSettings(annotations)
      case _: OpenIdConnectSettings => OpenIdConnectSettings(annotations)
      case _: Settings              => Settings(annotations)
    }
    cloned.adopted(parent)

    this.fields.foreach {
      case (f, v) =>
        val clonedValue = v.value match {
          case a: AmfArray =>
            domain.AmfArray(a.values.map {
              case s: Scope => s.cloneScope()
              case o        => o
            }, a.annotations)
          case o => o
        }

        cloned.set(f, clonedValue, v.annotations)
    }

    cloned.asInstanceOf[this.type]
  }

  override def meta: SettingsModel = SettingsModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = "/settings/default"
}

object Settings {

  def apply(): Settings = apply(Annotations())

  def apply(annotations: Annotations): Settings = new Settings(Fields(), annotations)
}

case class OAuth1Settings(override val fields: Fields, override val annotations: Annotations)
    extends Settings(fields, annotations) {

  def requestTokenUri: StrField     = fields.field(RequestTokenUri)
  def authorizationUri: StrField    = fields.field(AuthorizationUri1)
  def tokenCredentialsUri: StrField = fields.field(TokenCredentialsUri)
  def signatures: Seq[StrField]     = fields.field(Signatures)

  def withRequestTokenUri(requestTokenUri: String): this.type =
    set(RequestTokenUri, requestTokenUri)
  def withAuthorizationUri(authorizationUri: String): this.type =
    set(AuthorizationUri1, authorizationUri)
  def withTokenCredentialsUri(tokenCredentialsUri: String): this.type =
    set(TokenCredentialsUri, tokenCredentialsUri)
  def withSignatures(signatures: Seq[String]): this.type = set(Signatures, signatures)

  override def meta: OAuth1SettingsModel.type = OAuth1SettingsModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = "/settings/oauth1"
}

object OAuth1Settings {

  def apply(): OAuth1Settings = apply(Annotations())

  def apply(annotations: Annotations): OAuth1Settings = new OAuth1Settings(Fields(), annotations)
}

case class OAuth2Settings(override val fields: Fields, override val annotations: Annotations)
    extends Settings(fields, annotations) {

  def authorizationGrants: Seq[StrField] = fields.field(AuthorizationGrants)
  def flows: Seq[OAuth2Flow]             = fields.field(Flows)

  def withAuthorizationGrants(authorizationGrants: Seq[String]): this.type =
    set(AuthorizationGrants, authorizationGrants)
  def withFlows(flows: Seq[OAuth2Flow]): this.type =
    set(Flows, AmfArray(flows, Annotations.virtual()), Annotations.inferred())

  def withFlow(): OAuth2Flow = {
    val flow = OAuth2Flow()
    add(Flows, flow)
    flow
  }

  override def meta: OAuth2SettingsModel.type = OAuth2SettingsModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = "/settings/oauth2"
}

object OAuth2Settings {

  def apply(): OAuth2Settings = apply(Annotations())

  def apply(annotations: Annotations): OAuth2Settings = new OAuth2Settings(Fields(), annotations)
}

case class ApiKeySettings(override val fields: Fields, override val annotations: Annotations)
    extends Settings(fields, annotations) {

  def name: StrField = fields.field(Name)
  def in: StrField   = fields.field(In)

  def withName(name: String): this.type = set(Name, name)
  def withIn(in: String): this.type     = set(In, in)

  override def meta: ApiKeySettingsModel.type = ApiKeySettingsModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = "/settings/api-key"
}

object ApiKeySettings {

  def apply(): ApiKeySettings = apply(Annotations())

  def apply(annotations: Annotations): ApiKeySettings = new ApiKeySettings(Fields(), annotations)
}

case class HttpApiKeySettings(override val fields: Fields, override val annotations: Annotations)
    extends Settings(fields, annotations) {

  def name: StrField = fields.field(Name)
  def in: StrField   = fields.field(In)

  def withName(name: String): this.type = set(Name, name)
  def withIn(in: String): this.type     = set(In, in)

  override def meta: HttpApiKeySettingsModel.type = HttpApiKeySettingsModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = "/settings/http-api-key"
}

object HttpApiKeySettings {

  def apply(): HttpApiKeySettings = apply(Annotations())

  def apply(annotations: Annotations): HttpApiKeySettings = new HttpApiKeySettings(Fields(), annotations)
}

case class HttpSettings(override val fields: Fields, override val annotations: Annotations)
    extends Settings(fields, annotations) {

  def scheme: StrField       = fields.field(Scheme)
  def bearerFormat: StrField = fields.field(BearerFormat)

  def withScheme(scheme: String): this.type             = set(Scheme, scheme)
  def withBearerFormat(bearerFormat: String): this.type = set(BearerFormat, bearerFormat)

  override def meta: HttpSettingsModel.type = HttpSettingsModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = "/settings/http"
}

object HttpSettings {

  def apply(): HttpSettings = apply(Annotations())

  def apply(annotations: Annotations): HttpSettings = new HttpSettings(Fields(), annotations)
}

case class OpenIdConnectSettings(override val fields: Fields, override val annotations: Annotations)
    extends Settings(fields, annotations) {

  def url: StrField      = fields.field(Url)
  def scopes: Seq[Scope] = fields.field(OpenIdConnectSettingsModel.Scopes)

  def withUrl(url: String): this.type           = set(Url, url)
  def withScopes(scopes: Seq[Scope]): this.type = setArray(Scopes, scopes)

  override def meta: OpenIdConnectSettingsModel.type = OpenIdConnectSettingsModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = "/settings/open-id-connect"
}

object OpenIdConnectSettings {

  def apply(): OpenIdConnectSettings = apply(Annotations())

  def apply(annotations: Annotations): OpenIdConnectSettings = new OpenIdConnectSettings(Fields(), annotations)
}

trait WithSettings {
  def withDefaultSettings(): Settings
  def withOAuth1Settings(): OAuth1Settings
  def withOAuth2Settings(): OAuth2Settings
  def withApiKeySettings(): ApiKeySettings
  def withHttpSettings(): HttpSettings
  def withHttpApiKeySettings(): HttpApiKeySettings
  def withOpenIdConnectSettings(): OpenIdConnectSettings

  def id: String
}
