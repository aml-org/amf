package amf.apiinstance.internal.metamodel.domain

import amf.core.client.scala.vocabulary.Namespace.{ApiInstance, ApiManagement}
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.Str
import amf.core.internal.metamodel.domain.common.{DescriptionField, NameFieldSchema}
import amf.core.internal.metamodel.domain.templates.KeyField
import amf.core.internal.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}

trait BasePolicyModel extends DomainElementModel with NameFieldSchema with KeyField with DescriptionField {

  override val key: Field = Name

  val Namespace: Field = Field(Str,
    ApiManagement + "namespace",
    ModelDoc(ModelVocabularies.ApiManagement, "namespace", "Namespace for the policy"))

  val Category: Field = Field(Str,
    ApiManagement + "policyCategory",
    ModelDoc(ModelVocabularies.ApiManagement, "policyCategory", "Category this policy belongs to"))

  val baseType = ApiInstance + "Policy" :: DomainElementModel.`type`

  val baseFields = List(Namespace, Category) ++ DomainElementModel.fields
}
