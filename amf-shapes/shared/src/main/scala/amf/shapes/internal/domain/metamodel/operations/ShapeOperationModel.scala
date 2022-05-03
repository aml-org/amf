package amf.shapes.internal.domain.metamodel.operations

import amf.core.client.scala.vocabulary.Namespace.{Core, Shapes}
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.{Array, Str}
import amf.core.internal.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}
import amf.shapes.client.scala.model.domain.operations.ShapeOperation

object ShapeOperationModel extends AbstractOperationModel {

  override val key: Field = Name

  override val Method: Field = Field(
    Str,
    Shapes + "method",
    ModelDoc(ModelVocabularies.Shapes, "method", "Type of operation. It follows HTTP semantics"))

  override val Request: Field = Field(
    Array(ShapeRequestModel),
    Shapes + "expects",
    ModelDoc(ModelVocabularies.Shapes, "expects", "Request information required by the operation"))

  override val Responses: Field = Field(
    Array(ShapeResponseModel),
    Shapes + "returns",
    ModelDoc(ModelVocabularies.Shapes, "returns", "Response data returned by the operation"))

  override val `type`: List[ValueType] = Shapes + "Operation" :: Core + "Operation" :: DomainElementModel.`type`

  override val fields: List[Field] = List(
    Name,
    Description,
    Request,
    Responses,
  ) ++ DomainElementModel.fields

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Shapes,
    "ShapeOperation",
    "Action that can be executed over the data of a particular shape"
  )

  override def modelInstance: ShapeOperation = ShapeOperation()
}
