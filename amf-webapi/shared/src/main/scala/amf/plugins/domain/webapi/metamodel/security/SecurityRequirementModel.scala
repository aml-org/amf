package amf.plugins.domain.webapi.metamodel.security

import amf.core.metamodel.Field
import amf.core.metamodel.Type.Array
import amf.core.metamodel.domain.common.NameFieldSchema
import amf.core.metamodel.domain.templates.KeyField
import amf.core.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}
import amf.core.vocabulary.Namespace.Security
import amf.core.vocabulary.ValueType
import amf.plugins.domain.webapi.models.security.SecurityRequirement

object SecurityRequirementModel extends DomainElementModel with NameFieldSchema with KeyField {

  val Schemes =
    Field(Array(ParametrizedSecuritySchemeModel),
          Security + "schemes",
          ModelDoc(ModelVocabularies.Security, "schemes", ""))

  override val `type`: List[ValueType] = List(Security + "SecurityRequirement") ++ DomainElementModel.`type`

  override val fields: List[Field] =
    List(Schemes) ++ DomainElementModel.fields

  override def modelInstance = SecurityRequirement()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Security,
    "Security Requirement",
    "Flow for an OAuth2 security scheme setting"
  )
  override val key: Field = Name
}
