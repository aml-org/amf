package amf.shapes.internal.domain.metamodel.operations

import amf.core.client.scala.vocabulary.Namespace.{ApiContract, Shapes}
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.common.NameFieldSchema
import amf.core.internal.metamodel.domain.templates.KeyField
import amf.core.internal.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}
import amf.shapes.client.scala.model.domain.operations.ShapeResponse

object ShapeResponseModel extends DomainElementModel with KeyField with NameFieldSchema {

  val Payload: Field = Field(ShapePayloadModel,
                              ApiContract + "payload",
                              ModelDoc(ModelVocabularies.ApiContract, "payload", "Payload for a Request/Response"))

  override val key: Field = Name

  override val `type`: List[ValueType] = Shapes + "Response" :: DomainElementModel.`type`

  override val fields: List[Field] = List(Name) :+ Payload

  override def modelInstance = ShapeResponse()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Shapes,
    "Response",
    "Response information for an operation"
  )
}
