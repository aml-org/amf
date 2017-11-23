package amf.metadata.domain.security

import amf.domain.security.{ApiKeySettings, OAuth1Settings, OAuth2Settings, Settings}
import amf.framework.metamodel.Field
import amf.framework.metamodel.Type.{Array, Str}
import amf.metadata.domain.DomainElementModel
import amf.metadata.domain.extensions.DataNodeModel
import amf.vocabulary.Namespace.Security
import amf.vocabulary.ValueType

trait SettingsModel extends DomainElementModel

object SettingsModel extends SettingsModel {
  val AdditionalProperties = Field(DataNodeModel, Security + "additionalProperties")

  override val `type`: List[ValueType] = List(Security + "Settings") ++ DomainElementModel.`type`

  override def fields: List[Field] = List(AdditionalProperties) ++ DomainElementModel.fields

  override def modelInstance = Settings()
}

object OAuth1SettingsModel extends SettingsModel {

  val RequestTokenUri = Field(Str, Security + "requestTokenUri")

  val AuthorizationUri = Field(Str, Security + "authorizationUri")

  val TokenCredentialsUri = Field(Str, Security + "tokenCredentialsUri")

  val Signatures = Field(Array(Str), Security + "signatures")

  override val `type`: List[ValueType] = List(Security + "OAuth1Settings") ++ SettingsModel.`type`

  override def fields: List[Field] =
    List(RequestTokenUri, AuthorizationUri, TokenCredentialsUri, Signatures) ++ SettingsModel.fields

  override def modelInstance = OAuth1Settings()
}

object OAuth2SettingsModel extends SettingsModel {

  val AuthorizationUri = Field(Str, Security + "authorizationUri")

  val AccessTokenUri = Field(Str, Security + "accessTokenUri")

  val AuthorizationGrants = Field(Array(Str), Security + "authorizationGrants")

  val Flow = Field(Str, Security + "flow")

  val Scopes = Field(Array(ScopeModel), Security + "scopes")

  override val `type`: List[ValueType] = List(Security + "OAuth2Settings") ++ SettingsModel.`type`

  override def fields: List[Field] =
    List(AuthorizationUri, AccessTokenUri, AuthorizationGrants, Flow, Scopes) ++ SettingsModel.fields

  override def modelInstance = OAuth2Settings()
}

object ApiKeySettingsModel extends SettingsModel {

  val Name = Field(Str, Security + "name")

  val In = Field(Str, Security + "in")

  override val `type`: List[ValueType] = List(Security + "ApiKeySettings") ++ SettingsModel.`type`

  override def fields: List[Field] = List(Name, In) ++ SettingsModel.fields

  override def modelInstance = ApiKeySettings()
}
