package amf.shapes.internal.domain.metamodel.operations

import amf.core.client.scala.vocabulary.Namespace.{Core, Shapes}
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.Array
import amf.core.internal.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}
import amf.shapes.client.scala.model.domain.operations.ShapeRequest

object ShapeRequestModel extends AbstractRequestModel {

  override val QueryParameters: Field = Field(
    Array(ShapeParameterModel),
    Shapes + "parameter",
    ModelDoc(ModelVocabularies.Shapes, "parameter", "Parameters associated to the communication model")
  )

  override val `type`: List[ValueType] = Shapes + "Request" :: Core + "Request" :: DomainElementModel.`type`

  override def fields: List[Field] =
    List(QueryParameters)

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Shapes,
    "ShapeRequest",
    "Request information for an operation"
  )
  override val key: Field = Name

  override def modelInstance: ShapeRequest = ShapeRequest()

}
