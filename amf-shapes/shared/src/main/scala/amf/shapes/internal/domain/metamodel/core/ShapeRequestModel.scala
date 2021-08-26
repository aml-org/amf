package amf.shapes.internal.domain.metamodel.core

import amf.core.client.scala.vocabulary.Namespace.ApiContract
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.{Array, Bool}
import amf.core.internal.metamodel.domain.common.NameFieldSchema
import amf.core.internal.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}
import amf.core.internal.metamodel.domain.templates.KeyField
import amf.shapes.client.scala.model.domain.core.ShapeRequest

object ShapeRequestModel extends DomainElementModel with KeyField with NameFieldSchema {

  val QueryParameters = Field(
    Array(ShapeParameterModel),
    ApiContract + "parameter",
    ModelDoc(ModelVocabularies.ApiContract, "parameter", "Parameters associated to the communication model")
  )

  override val `type`: List[ValueType] = List(ApiContract + "Request")

  override def fields: List[Field] =
    List(QueryParameters)

  override def modelInstance = ShapeRequest()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiContract,
    "Request",
    "Request information for an operation"
  )
  override val key: Field = Name
}
