package amf.plugins.domain.apicontract.metamodel.security

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Str, Array}
import amf.core.metamodel.domain.templates.KeyField
import amf.core.metamodel.domain.{ModelDoc, ModelVocabularies, DomainElementModel}
import amf.core.vocabulary.Namespace.Security
import amf.core.vocabulary.ValueType
import amf.plugins.domain.apicontract.models.security.OAuth2Flow

object OAuth2FlowModel extends DomainElementModel with KeyField {

  val AuthorizationUri =
    Field(Str, Security + "authorizationUri", ModelDoc(ModelVocabularies.Security, "authorizationURI", ""))

  val AccessTokenUri =
    Field(Str, Security + "accessTokenUri", ModelDoc(ModelVocabularies.Security, "accessTokenURI", ""))

  val Flow = Field(Str, Security + "flow", ModelDoc(ModelVocabularies.Security, "flow", ""))

  val RefreshUri = Field(Str, Security + "refreshUri", ModelDoc(ModelVocabularies.Security, "refreshURI", ""))

  val Scopes = Field(Array(ScopeModel), Security + "scope", ModelDoc(ModelVocabularies.Security, "scope", ""))

  override val `type`: List[ValueType] = List(Security + "OAuth2Flow") ++ DomainElementModel.`type`

  override val fields: List[Field] =
    List(AuthorizationUri, AccessTokenUri, Flow, RefreshUri, Scopes) ++ DomainElementModel.fields

  override def modelInstance = OAuth2Flow()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Security,
    "OAuth2Flow",
    "Flow for an OAuth2 security scheme setting"
  )

  override val key: Field = Flow
}
