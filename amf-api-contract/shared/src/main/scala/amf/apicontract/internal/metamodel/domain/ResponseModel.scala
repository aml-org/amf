package amf.apicontract.internal.metamodel.domain

import amf.apicontract.client.scala.model.domain.Response
import amf.core.client.scala.model.domain.AmfObject
import amf.core.client.scala.vocabulary.Namespace.{ApiContract, Core}
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.{Array, Str}
import amf.core.internal.metamodel.domain.templates.OptionalField
import amf.core.internal.metamodel.domain.{ModelDoc, ModelVocabularies}
import amf.shapes.internal.domain.metamodel.operations.AbstractResponseModel

/**
  * Response metamodel.
  */
object ResponseModel extends AbstractResponseModel with MessageModel with OptionalField {
  override val Payload: Field = Field(PayloadModel,
    ApiContract + "payload",
    ModelDoc(ModelVocabularies.ApiContract, "payload", "Payload for a Request/Response"))

  val StatusCode: Field = Field(
    Str,
    ApiContract + "statusCode",
    ModelDoc(ModelVocabularies.ApiContract, "statusCode", "HTTP status code returned by a response"))

  val Links: Field = Field(
    Array(TemplatedLinkModel),
    ApiContract + "link",
    ModelDoc(ModelVocabularies.ApiContract, "links", "Structural definition of links on the source data shape AST")
  )

  override val key: Field = StatusCode

  override val `type`: List[ValueType] = ApiContract + "Response" :: Core + "Response" :: MessageModel.`type`

  override val fields: List[Field] =
    List(StatusCode, Links) ++ MessageModel.fields

  override def modelInstance: AmfObject = Response()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiContract,
    "Response",
    "Response information for an operation"
  )
}
