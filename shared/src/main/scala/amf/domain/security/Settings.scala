package amf.domain.security

import amf.domain.extensions.DataNode
import amf.domain.{DomainElement, Fields}
import amf.framework.parser.Annotations
import amf.metadata.domain.security.ApiKeySettingsModel._
import amf.metadata.domain.security.OAuth1SettingsModel.{AuthorizationUri => AuthorizationUri1, _}
import amf.metadata.domain.security.OAuth2SettingsModel.{AuthorizationUri => AuthorizationUri2, _}
import amf.metadata.domain.security.SettingsModel._
import amf.model.AmfArray

class Settings(val fields: Fields, val annotations: Annotations) extends DomainElement {
  def additionalProperties: DataNode = fields(AdditionalProperties)

  def withAdditionalProperties(additionalProperties: DataNode): this.type =
    set(AdditionalProperties, additionalProperties)

  override def adopted(parent: String): this.type = withId(parent + "/settings/default")

  def cloneSettings(parent: String): Settings = {
    val cloned = this match {
      case _: OAuth1Settings => OAuth1Settings(annotations)
      case _: OAuth2Settings => OAuth2Settings(annotations)
      case _: ApiKeySettings => ApiKeySettings(annotations)
      case _: Settings       => Settings(annotations)
    }
    cloned.adopted(parent)

    this.fields.foreach {
      case (f, v) =>
        val clonedValue = v.value match {
          case a: AmfArray =>
            AmfArray(a.values.map {
              case s: Scope => s.cloneScope()
              case o        => o
            }, a.annotations)
          case o => o
        }

        cloned.set(f, clonedValue, v.annotations)
    }

    cloned.asInstanceOf[this.type]
  }
}

object Settings {

  def apply(): Settings = apply(Annotations())

  def apply(annotations: Annotations): Settings = new Settings(Fields(), annotations)
}

case class OAuth1Settings(override val fields: Fields, override val annotations: Annotations)
    extends Settings(fields, annotations) {
  def requestTokenUri: String     = fields(RequestTokenUri)
  def authorizationUri: String    = fields(AuthorizationUri1)
  def tokenCredentialsUri: String = fields(TokenCredentialsUri)
  def signatures: Seq[String]     = fields(Signatures)

  def withRequestTokenUri(requestTokenUri: String): this.type =
    set(RequestTokenUri, requestTokenUri)
  def withAuthorizationUri(authorizationUri: String): this.type =
    set(AuthorizationUri1, authorizationUri)
  def withTokenCredentialsUri(tokenCredentialsUri: String): this.type =
    set(TokenCredentialsUri, tokenCredentialsUri)
  def withSignatures(signatures: Seq[String]): this.type = set(Signatures, signatures)

  override def adopted(parent: String): this.type = withId(parent + "/settings/oauth1")
}

object OAuth1Settings {

  def apply(): OAuth1Settings = apply(Annotations())

  def apply(annotations: Annotations): OAuth1Settings = new OAuth1Settings(Fields(), annotations)
}

case class OAuth2Settings(override val fields: Fields, override val annotations: Annotations)
    extends Settings(fields, annotations) {
  def authorizationUri: String         = fields(AuthorizationUri2)
  def accessTokenUri: String           = fields(AccessTokenUri)
  def authorizationGrants: Seq[String] = fields(AuthorizationGrants)
  def flow: String                     = fields(Flow)
  def scopes: Seq[Scope]               = fields(Scopes)

  def withAuthorizationUri(authorizationUri: String): this.type =
    set(AuthorizationUri2, authorizationUri)
  def withAccessTokenUri(accessTokenUri: String): this.type = set(AccessTokenUri, accessTokenUri)
  def withAuthorizationGrants(authorizationGrants: Seq[String]): this.type =
    set(AuthorizationGrants, authorizationGrants)
  def withFlow(flow: String): this.type         = set(Flow, flow)
  def withScopes(scopes: Seq[Scope]): this.type = setArray(Scopes, scopes)

  override def adopted(parent: String): this.type = withId(parent + "/settings/oauth2")
}

object OAuth2Settings {

  def apply(): OAuth2Settings = apply(Annotations())

  def apply(annotations: Annotations): OAuth2Settings = new OAuth2Settings(Fields(), annotations)
}

case class ApiKeySettings(override val fields: Fields, override val annotations: Annotations)
    extends Settings(fields, annotations) {
  def name: String = fields(Name)
  def in: String   = fields(In)

  def withName(name: String): this.type = set(Name, name)
  def withIn(in: String): this.type     = set(In, in)

  override def adopted(parent: String): this.type = withId(parent + "/settings/api-key")
}

object ApiKeySettings {

  def apply(): ApiKeySettings = apply(Annotations())

  def apply(annotations: Annotations): ApiKeySettings = new ApiKeySettings(Fields(), annotations)
}

trait WithSettings {
  def withDefaultSettings(): Settings
  def withOAuth1Settings(): OAuth1Settings
  def withOAuth2Settings(): OAuth2Settings
  def withApiKeySettings(): ApiKeySettings

  def id: String
}
