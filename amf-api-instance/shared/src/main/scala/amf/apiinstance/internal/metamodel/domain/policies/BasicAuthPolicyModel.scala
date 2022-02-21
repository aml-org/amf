package amf.apiinstance.internal.metamodel.domain.policies

import amf.apiinstance.client.scala.model.domain.policies.BasicAuthPolicy
import amf.apiinstance.internal.metamodel.domain.BasePolicyModel
import amf.core.client.scala.model.domain.AmfObject
import amf.core.client.scala.vocabulary.Namespace.ApiManagement
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.Str
import amf.core.internal.metamodel.domain.{ModelDoc, ModelVocabularies}

object BasicAuthPolicyModel extends BasePolicyModel  {

  val Username: Field = Field(Str,
    ApiManagement + "username",
    ModelDoc(ModelVocabularies.ApiInstance, "policyCategory", "Category this policy belongs to"))

  val Password: Field = Field(Str,
    ApiManagement + "password",
    ModelDoc(ModelVocabularies.ApiInstance, "policyCategory", "Category this policy belongs to"))


  override def modelInstance: AmfObject = BasicAuthPolicy()

  override val `type`: List[ValueType] = ApiManagement + "BasicAuthPolicy" :: baseType

  override def fields: List[Field] = List(
    Username,
    Password
  ) ++ baseFields
}
