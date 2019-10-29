package amf.plugins.domain.webapi.models.security

import amf.core.metamodel.Obj
import amf.core.model.domain.{AmfArray, DataNode, DomainElement}
import amf.core.model.{StrField, domain}
import amf.core.parser.{Annotations, Fields}
import amf.plugins.domain.webapi.metamodel.security.ApiKeySettingsModel._
import amf.plugins.domain.webapi.metamodel.security.HttpSettingsModel._
import amf.plugins.domain.webapi.metamodel.security.OAuth1SettingsModel.{AuthorizationUri => AuthorizationUri1, _}
import amf.plugins.domain.webapi.metamodel.security.OAuth2SettingsModel._
import amf.plugins.domain.webapi.metamodel.security.OpenIdConnectSettingsModel._
import amf.plugins.domain.webapi.metamodel.security.SettingsModel._
import amf.plugins.domain.webapi.metamodel.security._

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

  override def meta: Obj = SettingsModel

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

  override def meta: Obj = OAuth1SettingsModel

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
  def withFlows(flows: Seq[OAuth2Flow]): this.type = setArray(Flows, flows)

  def withFlow(): OAuth2Flow = {
    val flow = OAuth2Flow()
    add(OAuth2FlowModel.Flow, flow)
    flow
  }

  override def meta: Obj = OAuth2SettingsModel

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

  override def meta: Obj = ApiKeySettingsModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = "/settings/api-key"
}

object ApiKeySettings {

  def apply(): ApiKeySettings = apply(Annotations())

  def apply(annotations: Annotations): ApiKeySettings = new ApiKeySettings(Fields(), annotations)
}

case class HttpSettings(override val fields: Fields, override val annotations: Annotations)
    extends Settings(fields, annotations) {

  def scheme: StrField       = fields.field(Scheme)
  def bearerFormat: StrField = fields.field(BearerFormat)

  def withScheme(scheme: String): this.type             = set(Scheme, scheme)
  def withBearerFormat(bearerFormat: String): this.type = set(BearerFormat, bearerFormat)

  override def meta: Obj = HttpSettingsModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = "/settings/http"
}

object HttpSettings {

  def apply(): HttpSettings = apply(Annotations())

  def apply(annotations: Annotations): HttpSettings = new HttpSettings(Fields(), annotations)
}

case class OpenIdConnectSettings(override val fields: Fields, override val annotations: Annotations)
    extends Settings(fields, annotations) {

  def url: StrField = fields.field(Url)

  def withUrl(url: String): this.type = set(Url, url)

  override def meta: Obj = OpenIdConnectSettingsModel

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
  def withOpenIdConnectSettings(): OpenIdConnectSettings

  def id: String
}
