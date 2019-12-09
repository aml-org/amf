package amf.plugins.domain.webapi.metamodel.security

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Array, Str}
import amf.core.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}
import amf.core.vocabulary.Namespace.Security
import amf.core.vocabulary.ValueType
import amf.plugins.domain.webapi.models.security.OAuth2Flow

object OAuth2FlowModel extends DomainElementModel {

  val AuthorizationUri =
    Field(Str, Security + "authorizationUri", ModelDoc(ModelVocabularies.Security, "authorization URI", ""))

  val AccessTokenUri =
    Field(Str, Security + "accessTokenUri", ModelDoc(ModelVocabularies.Security, "access token URI", ""))

  val Flow = Field(Str, Security + "flow", ModelDoc(ModelVocabularies.Security, "flow", ""))

  val RefreshUri = Field(Str, Security + "refreshUri", ModelDoc(ModelVocabularies.Security, "refresh URI", ""))

  val Scopes = Field(Array(ScopeModel), Security + "scope", ModelDoc(ModelVocabularies.Security, "scope", ""))

  override val `type`: List[ValueType] = List(Security + "OAuth2Flow") ++ DomainElementModel.`type`

  override val fields: List[Field] =
    List(AuthorizationUri, AccessTokenUri, Flow, RefreshUri, Scopes) ++ DomainElementModel.fields

  override def modelInstance = OAuth2Flow()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Security,
    "OAuth2 Flow",
    "Flow for an OAuth2 security scheme setting"
  )
}
