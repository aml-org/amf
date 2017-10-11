package amf.domain.security

import amf.domain.{Annotations, DomainElement, Fields}
import amf.metadata.domain.security.{OAuth1SettingsModel, OAuth2SettingsModel, ApiKeySettingsModel}

abstract class Settings(fields: Fields, annotations: Annotations) extends DomainElement

case class OAuth1Settings(fields: Fields, annotations: Annotations) extends Settings(fields, annotations) {
  def requestTokenUri: String     = fields(OAuth1SettingsModel.RequestTokenUri)
  def authorizationUri: String    = fields(OAuth1SettingsModel.AuthorizationUri)
  def tokenCredentialsUri: String = fields(OAuth1SettingsModel.TokenCredentialsUri)
  def signatures: Seq[String]     = fields(OAuth1SettingsModel.Signatures)

  def withRequestTokenUri(requestTokenUri: String): this.type =
    set(OAuth1SettingsModel.RequestTokenUri, requestTokenUri)
  def withAuthorizationUri(authorizationUri: String): this.type =
    set(OAuth1SettingsModel.AuthorizationUri, authorizationUri)
  def withTokenCredentialsUri(tokenCredentialsUri: String): this.type =
    set(OAuth1SettingsModel.TokenCredentialsUri, tokenCredentialsUri)
  def withSignatures(signatures: Seq[String]): this.type = set(OAuth1SettingsModel.Signatures, signatures)

  override def adopted(parent: String): this.type = withId(parent + "/settings/oauth1")
}

object OAuth1Settings {

  def apply(): OAuth1Settings = apply(Annotations())

  def apply(annotations: Annotations): OAuth1Settings = new OAuth1Settings(Fields(), annotations)
}

case class OAuth2Settings(fields: Fields, annotations: Annotations) extends Settings(fields, annotations) {
  def authorizationUri: String    = fields(OAuth2SettingsModel.AuthorizationUri)
  def accessTokenUri: String      = fields(OAuth2SettingsModel.AccessTokenUri)
  def authorizationGrants: String = fields(OAuth2SettingsModel.AuthorizationGrants)
  def flow: String                = fields(OAuth2SettingsModel.Flow)
  def scopes: Seq[Scope]          = fields(OAuth2SettingsModel.Scopes)

  def withAuthorizationUri(authorizationUri: String): this.type =
    set(OAuth2SettingsModel.AuthorizationUri, authorizationUri)
  def withAccessTokenUri(accessTokenUri: String): this.type = set(OAuth2SettingsModel.AccessTokenUri, accessTokenUri)
  def withAuthorizationGrants(authorizationGrants: String): this.type =
    set(OAuth2SettingsModel.AuthorizationGrants, authorizationGrants)
  def withFlow(flow: String): this.type         = set(OAuth2SettingsModel.Flow, flow)
  def withScopes(scopes: Seq[Scope]): this.type = setArray(OAuth2SettingsModel.Scopes, scopes)

  override def adopted(parent: String): this.type = withId(parent + "/settings/oauth1")
}

object OAuth2Settings {

  def apply(): OAuth2Settings = apply(Annotations())

  def apply(annotations: Annotations): OAuth2Settings = new OAuth2Settings(Fields(), annotations)
}

case class ApiKeySettings(fields: Fields, annotations: Annotations) extends Settings(fields, annotations) {
  def name: String = fields(ApiKeySettingsModel.Name)
  def in: String   = fields(ApiKeySettingsModel.In)

  def withName(name: String): this.type = set(ApiKeySettingsModel.Name, name)
  def withIn(in: String): this.type     = set(ApiKeySettingsModel.In, in)

  override def adopted(parent: String): this.type = withId(parent + "/settings/oauth1")
}

object ApiKeySettings {

  def apply(): ApiKeySettings = apply(Annotations())

  def apply(annotations: Annotations): ApiKeySettings = new ApiKeySettings(Fields(), annotations)
}
