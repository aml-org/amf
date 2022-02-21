package amf.apiinstance.internal.metamodel.domain

import amf.apiinstance.client.scala.model.domain.Route
import amf.core.client.scala.model.domain.AmfObject
import amf.core.client.scala.vocabulary.Namespace.{ApiContract, ApiInstance}
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.{Array, Str}
import amf.core.internal.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}

object RouteModel extends DomainElementModel {
  override def modelInstance: AmfObject = Route()

  val Path =
    Field(Str, ApiContract + "path", ModelDoc(ModelVocabularies.ApiContract, "path", "Path template for an endpoint"))

  val Rule: Field = Field(Array(FilterRuleModel),
    ApiInstance + "rule",
    ModelDoc(ModelVocabularies.ApiInstance, "rule", "Rules that must be checked before applying this filter chain to a input request"))

  override val `type`: List[ValueType] = ApiInstance + "Route" :: DomainElementModel.`type`

  override def fields: List[Field] = List(
    Path,
    Rule
  )  ++ DomainElementModel.fields
}
