package amf.shapes.internal.domain.metamodel.operations

import amf.core.client.scala.vocabulary.Namespace.{Core, Shapes}
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}
import amf.shapes.client.scala.model.domain.operations.ShapeResponse

object ShapeResponseModel extends AbstractResponseModel {

  override val Payload: Field = Field(ShapePayloadModel,
                                      Shapes + "payload",
                                      ModelDoc(ModelVocabularies.Shapes, "payload", "Payload for a Request/Response"))

  override val key: Field = Name

  override val `type`: List[ValueType] = Shapes + "Response" :: Core + "Response" :: DomainElementModel.`type`

  override val fields: List[Field] = List(Name) :+ Payload

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Shapes,
    "ShapeResponse",
    "Response information for an operation"
  )

  override def modelInstance: ShapeResponse = ShapeResponse()

}
