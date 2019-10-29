package amf.plugins.domain.webapi.metamodel.security

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Array, Str}
import amf.core.metamodel.domain.{DataNodeModel, DomainElementModel, ModelDoc, ModelVocabularies}
import amf.plugins.domain.webapi.models.security.{ApiKeySettings, OAuth1Settings, OAuth2Settings, Settings}
import amf.core.vocabulary.Namespace.{Core, Security}
import amf.core.vocabulary.ValueType

trait SettingsModel extends DomainElementModel

object SettingsModel extends SettingsModel {
  val AdditionalProperties = Field(DataNodeModel,
                                   Security + "additionalProperties",
                                   ModelDoc(ModelVocabularies.Security, "additional properties", ""))

  override val `type`: List[ValueType] = List(Security + "Settings") ++ DomainElementModel.`type`

  override val fields: List[Field] = List(AdditionalProperties) ++ DomainElementModel.fields

  override def modelInstance = Settings()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Security,
    "Settings",
    "Settings for a security scheme"
  )
}

object OAuth1SettingsModel extends SettingsModel {

  val RequestTokenUri =
    Field(Str, Security + "requestTokenUri", ModelDoc(ModelVocabularies.Security, "request token URI", ""))

  val AuthorizationUri =
    Field(Str, Security + "authorizationUri", ModelDoc(ModelVocabularies.Security, "authorization URI", ""))

  val TokenCredentialsUri =
    Field(Str, Security + "tokenCredentialsUri", ModelDoc(ModelVocabularies.Security, "token credentials URI", ""))

  val Signatures = Field(Array(Str), Security + "signature", ModelDoc(ModelVocabularies.Security, "signature", ""))

  override val `type`: List[ValueType] = List(Security + "OAuth1Settings") ++ SettingsModel.`type`

  override def fields: List[Field] =
    List(RequestTokenUri, AuthorizationUri, TokenCredentialsUri, Signatures) ++ SettingsModel.fields

  override def modelInstance = OAuth1Settings()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Security,
    "OAuth1 Settings",
    "Settings for an OAuth1 security scheme"
  )
}

object OAuth2SettingsModel extends SettingsModel {

  val AuthorizationGrants =
    Field(Array(Str), Security + "authorizationGrant", ModelDoc(ModelVocabularies.Security, "authorization grant", ""))

  val Flows = Field(Array(OAuth2FlowModel), Security + "flows", ModelDoc(ModelVocabularies.Security, "flows", ""))

  override val `type`: List[ValueType] = List(Security + "OAuth2Settings") ++ SettingsModel.`type`

  override val fields: List[Field] =
    List(AuthorizationGrants, Flows) ++ SettingsModel.fields

  override def modelInstance = OAuth2Settings()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Security,
    "OAuth2 Settings",
    "Settings for an OAuth2 security scheme"
  )
}

object ApiKeySettingsModel extends SettingsModel {

  val Name = Field(Str, Core + "name", ModelDoc(ModelVocabularies.Security, "name", ""))

  val In = Field(Str, Security + "in", ModelDoc(ModelVocabularies.Security, "in", ""))

  override val `type`: List[ValueType] = List(Security + "ApiKeySettings") ++ SettingsModel.`type`

  override val fields: List[Field] = List(Name, In) ++ SettingsModel.fields

  override def modelInstance = ApiKeySettings()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Security,
    "API Key Settings",
    "Settings for an API Key security scheme"
  )
}

object HttpSettingsModel extends SettingsModel {

  val Scheme = Field(Str, Security + "scheme", ModelDoc(ModelVocabularies.Security, "scheme", ""))

  val BearerFormat = Field(Str, Security + "bearerFormat", ModelDoc(ModelVocabularies.Security, "bearer format", ""))

  override val `type`: List[ValueType] = List(Security + "HttpSettings") ++ SettingsModel.`type`

  override val fields: List[Field] = List(Scheme, BearerFormat) ++ SettingsModel.fields

  override def modelInstance = ApiKeySettings()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Security,
    "HTTP Settings",
    "Settings for an HTTP security scheme"
  )
}

object OpenIdConnectSettingsModel extends SettingsModel {

  val Url = Field(Str, Security + "openIdConnectUrl", ModelDoc(ModelVocabularies.Security, "OpenID connect URL", ""))

  override val `type`: List[ValueType] = List(Security + "OpenIdConnectSettings") ++ SettingsModel.`type`

  override def fields: List[Field] = List(Url) ++ SettingsModel.fields

  override def modelInstance = ApiKeySettings()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Security,
    "OpenID Settings",
    "Settings for an OpenID security scheme"
  )
}
