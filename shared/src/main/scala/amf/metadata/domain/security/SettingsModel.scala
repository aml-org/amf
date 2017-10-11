package amf.metadata.domain.security

import amf.metadata.Field
import amf.metadata.Type.{Array, Str}
import amf.metadata.domain.DomainElementModel
import amf.vocabulary.Namespace.Security
import amf.vocabulary.ValueType

trait SettingsModel extends DomainElementModel {

  override val `type`: List[ValueType] = List(Security + "Settings")
}

object SettingsModel extends SettingsModel {

  override def fields: List[Field] = DomainElementModel.fields
}

object OAuth1SettingsModel extends SettingsModel {

  val RequestTokenUri = Field(Str, Security + "requestTokenUri")

  val AuthorizationUri = Field(Str, Security + "authorizationUri")

  val TokenCredentialsUri = Field(Str, Security + "tokenCredentialsUri")

  val Signatures = Field(Array(Str), Security + "signatures")

  override def fields: List[Field] =
    List(RequestTokenUri, AuthorizationUri, TokenCredentialsUri, Signatures) ++ SettingsModel.fields
}

object OAuth2SettingsModel extends SettingsModel {

  val AuthorizationUri = Field(Str, Security + "authorizationUri")

  val AccessTokenUri = Field(Str, Security + "accessTokenUri")

  val AuthorizationGrants = Field(Str, Security + "authorizationGrants")

  val Flow = Field(Str, Security + "flow")

  val Scopes = Field(Array(Str), Security + "scopes")

  override def fields: List[Field] =
    List(AuthorizationUri, AccessTokenUri, AuthorizationGrants, Flow, Scopes) ++ SettingsModel.fields
}

object ApiKeySettingsModel extends SettingsModel {

  val Name = Field(Str, Security + "name")

  val In = Field(Str, Security + "in")

  val AuthorizationGrants = Field(Str, Security + "authorizationGrants")

  val Scopes = Field(Array(Str), Security + "scopes")

  override def fields: List[Field] = List(Name, In, AuthorizationGrants, Scopes) ++ SettingsModel.fields
}
