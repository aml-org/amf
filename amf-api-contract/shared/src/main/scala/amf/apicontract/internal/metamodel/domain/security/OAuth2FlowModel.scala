package amf.apicontract.internal.metamodel.domain.security

import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.{Array, Bool, Str}
import amf.core.internal.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}
import amf.core.internal.metamodel.domain.templates.KeyField
import amf.apicontract.client.scala.model.domain.security.OAuth2Flow
import amf.core.client.scala.vocabulary.Namespace.Security
import amf.core.client.scala.vocabulary.ValueType

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
