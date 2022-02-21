package amf.apiinstance.internal.metamodel.domain

import amf.apiinstance.client.scala.model.domain.FilterChain
import amf.core.client.scala.model.domain.AmfObject
import amf.core.client.scala.vocabulary.Namespace.ApiInstance
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.Array
import amf.core.internal.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}

object FilterChainModel extends DomainElementModel {
  override def modelInstance: AmfObject = FilterChain()

  val Rule: Field = Field(Array(FilterRuleModel),
    ApiInstance + "rule",
    ModelDoc(ModelVocabularies.ApiInstance, "rule", "Rules that must be checked before applying this filter chain to a input request"))

  val Policies: Field = Field(Array(DomainElementModel),
    ApiInstance + "policy",
    ModelDoc(ModelVocabularies.ApiInstance, "policy", "Rules that must be checked before applying this filter chain to a input request"))


  override val `type`: List[ValueType] = ApiInstance + "FilterChain" :: DomainElementModel.`type`

  override def fields: List[Field] = List(
    Rule,
    Policies
  ) ++ DomainElementModel.fields
}
