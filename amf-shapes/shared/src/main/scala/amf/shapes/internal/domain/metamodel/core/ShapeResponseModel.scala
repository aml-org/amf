package amf.shapes.internal.domain.metamodel.core

import amf.core.client.scala.vocabulary.Namespace.ApiContract
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.{Array, Str}
import amf.core.internal.metamodel.domain.common.NameFieldSchema
import amf.core.internal.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}
import amf.core.internal.metamodel.domain.templates.{KeyField, OptionalField}
import amf.shapes.client.scala.model.domain.core.ShapeResponse

object ShapeResponseModel extends DomainElementModel with KeyField with NameFieldSchema {

  val StatusCode = Field(
    Str,
    ApiContract + "statusCode",
    ModelDoc(ModelVocabularies.ApiContract, "statusCode", "HTTP status code returned by a response"))

  val Payloads: Field = Field(Array(ShapePayloadModel),
                              ApiContract + "payload",
                              ModelDoc(ModelVocabularies.ApiContract, "payload", "Payload for a Request/Response"))

  override val key: Field = StatusCode

  override val `type`: List[ValueType] = ApiContract + "Response" :: DomainElementModel.`type`

  override val fields: List[Field] =
    List(StatusCode) :+ Payloads

  override def modelInstance = ShapeResponse()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiContract,
    "Response",
    "Response information for an operation"
  )
}
