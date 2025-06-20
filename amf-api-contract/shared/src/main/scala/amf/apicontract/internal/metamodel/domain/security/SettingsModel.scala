package amf.apicontract.internal.metamodel.domain.security

import amf.apicontract.client.scala.model.domain.security._
import amf.core.client.scala.model.domain.AmfObject
import amf.core.client.scala.vocabulary.Namespace.{Core, Security}
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.{Array, Str}
import amf.core.internal.metamodel.domain.{DataNodeModel, DomainElementModel, ModelDoc, ModelVocabularies}

trait SettingsModel extends DomainElementModel

trait WithScopesField {
  val Scopes: Field = Field(Array(ScopeModel), Security + "scope", ModelDoc(ModelVocabularies.Security, "scope", ""))
}

object SettingsModel extends SettingsModel {
  val AdditionalProperties: Field = Field(
    DataNodeModel,
    Security + "additionalProperties",
    ModelDoc(ModelVocabularies.Security, "additionalProperties", "")
  )

  override val `type`: List[ValueType] = List(Security + "Settings") ++ DomainElementModel.`type`

  override val fields: List[Field] = List(AdditionalProperties) ++ DomainElementModel.fields

  override def modelInstance: AmfObject = Settings()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Security,
    "Settings",
    "Settings for a security scheme"
  )
}

object OAuth1SettingsModel extends SettingsModel {

  val RequestTokenUri: Field =
    Field(Str, Security + "requestTokenUri", ModelDoc(ModelVocabularies.Security, "requestTokenURI", ""))

  val AuthorizationUri: Field =
    Field(Str, Security + "authorizationUri", ModelDoc(ModelVocabularies.Security, "authorizationURI", ""))

  val TokenCredentialsUri: Field =
    Field(Str, Security + "tokenCredentialsUri", ModelDoc(ModelVocabularies.Security, "tokenCredentialsURI", ""))

  val Signatures: Field =
    Field(Array(Str), Security + "signature", ModelDoc(ModelVocabularies.Security, "signature", ""))

  override val `type`: List[ValueType] = List(Security + "OAuth1Settings") ++ SettingsModel.`type`

  override def fields: List[Field] =
    List(RequestTokenUri, AuthorizationUri, TokenCredentialsUri, Signatures) ++ SettingsModel.fields

  override def modelInstance: AmfObject = OAuth1Settings()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Security,
    "OAuth1Settings",
    "Settings for an OAuth1 security scheme"
  )
}

object OAuth2SettingsModel extends SettingsModel {

  val AuthorizationGrants: Field =
    Field(Array(Str), Security + "authorizationGrant", ModelDoc(ModelVocabularies.Security, "authorizationGrant", ""))

  val Flows: Field =
    Field(Array(OAuth2FlowModel), Security + "flows", ModelDoc(ModelVocabularies.Security, "flows", ""))

  override val `type`: List[ValueType] = List(Security + "OAuth2Settings") ++ SettingsModel.`type`

  override val fields: List[Field] =
    List(AuthorizationGrants, Flows) ++ SettingsModel.fields

  override def modelInstance: AmfObject = OAuth2Settings()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Security,
    "OAuth2Settings",
    "Settings for an OAuth2 security scheme"
  )
}

object ApiKeySettingsModel extends SettingsModel with WithScopesField {

  val Name: Field = Field(Str, Core + "name", ModelDoc(ModelVocabularies.Security, "name", ""))

  val In: Field = Field(Str, Security + "in", ModelDoc(ModelVocabularies.Security, "in", ""))

  override val `type`: List[ValueType] = List(Security + "ApiKeySettings") ++ SettingsModel.`type`

  override val fields: List[Field] = List(Name, In, Scopes) ++ SettingsModel.fields

  override def modelInstance: AmfObject = ApiKeySettings()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Security,
    "APIKeySettings",
    "Settings for an API Key security scheme"
  )
}

object HttpApiKeySettingsModel extends SettingsModel {

  val Name: Field = Field(Str, Core + "name", ModelDoc(ModelVocabularies.Security, "name", ""))

  val In: Field = Field(Str, Security + "in", ModelDoc(ModelVocabularies.Security, "in", ""))

  override val `type`: List[ValueType] = List(Security + "HttpApiKeySettings") ++ SettingsModel.`type`

  override val fields: List[Field] = List(Name, In) ++ SettingsModel.fields

  override def modelInstance: AmfObject = HttpApiKeySettings()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Security,
    "HttpAPIKeySettings",
    "Settings for an Http API Key security scheme"
  )
}

object HttpSettingsModel extends SettingsModel with WithScopesField {

  val Scheme: Field = Field(Str, Security + "scheme", ModelDoc(ModelVocabularies.Security, "scheme", ""))

  val BearerFormat: Field =
    Field(Str, Security + "bearerFormat", ModelDoc(ModelVocabularies.Security, "bearerFormat", ""))

  override val `type`: List[ValueType] = List(Security + "HttpSettings") ++ SettingsModel.`type`

  override val fields: List[Field] = List(Scheme, BearerFormat, Scopes) ++ SettingsModel.fields

  override def modelInstance: AmfObject = HttpSettings()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Security,
    "HttpSettings",
    "Settings for an HTTP security scheme"
  )
}

object OpenIdConnectSettingsModel extends SettingsModel with WithScopesField {

  val Url: Field =
    Field(Str, Security + "openIdConnectUrl", ModelDoc(ModelVocabularies.Security, "openIdConnectUrl", ""))

  override val `type`: List[ValueType] = List(Security + "OpenIdConnectSettings") ++ SettingsModel.`type`

  override def fields: List[Field] = List(Url, Scopes) ++ SettingsModel.fields

  override def modelInstance: OpenIdConnectSettings = OpenIdConnectSettings()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Security,
    "OpenIDSettings",
    "Settings for an OpenID security scheme"
  )
}

object MutualTLSSettingsModel extends SettingsModel with WithScopesField {

  override val `type`: List[ValueType] = List(Security + "MutualTLSSettings") ++ SettingsModel.`type`

  override val fields: List[Field] = List(Scopes) ++ SettingsModel.fields

  override def modelInstance: AmfObject = MutualTLSSettings()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Security,
    "MutualTLSSettings",
    "Settings for an Mutual TLS security scheme"
  )
}
