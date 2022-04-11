package amf.apicontract.internal.metamodel.domain

import amf.apicontract.client.scala.model.domain.Request
import amf.core.client.scala.model.domain.AmfObject
import amf.core.client.scala.vocabulary.Namespace.{ApiContract, Core}
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.{Array, Bool}
import amf.core.internal.metamodel.domain.{ModelDoc, ModelVocabularies}
import amf.shapes.internal.domain.metamodel.operations.AbstractRequestModel

/**
  * Request metaModel.
  */
object RequestModel extends AbstractRequestModel with MessageModel {

  override val QueryParameters: Field = Field(
    Array(ParameterModel),
    ApiContract + "parameter",
    ModelDoc(ModelVocabularies.ApiContract, "parameter", "Parameters associated to the communication model")
  )

  val Required: Field = Field(Bool,
                       ApiContract + "required",
                       ModelDoc(ModelVocabularies.ApiContract, "required", "Marks the parameter as required"))

  val CookieParameters: Field =
    Field(Array(ParameterModel),
          ApiContract + "cookieParameter",
          ModelDoc(ModelVocabularies.ApiContract, "cookieParameter", ""))

  override val `type`: List[ValueType] = ApiContract + "Request" :: Core + "Request" :: MessageModel.`type`

  override def fields: List[Field] =
    List(Required, QueryParameters, QueryString, UriParameters, CookieParameters) ++ MessageModel.fields

  override def modelInstance: AmfObject = Request()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiContract,
    "Request",
    "Request information for an operation"
  )
  override val key: Field = Name
}
