package amf.plugins.domain.apicontract.metamodel.security

import amf.plugins.domain.apicontract.models.security.SecurityRequirement
import amf.core.client.scala.vocabulary.Namespace.{Core, Security}
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.common.NameFieldSchema
import amf.core.internal.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}
import amf.core.internal.metamodel.domain.templates.KeyField

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
    "SecurityRequirement",
    "Flow for an OAuth2 security scheme setting"
  )
  override val key: Field = Name
}
