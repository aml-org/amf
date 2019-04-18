package amf.plugins.domain.webapi.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Array, Bool}
import amf.core.metamodel.domain.common.{DescriptionField, NameFieldSchema}
import amf.core.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}
import amf.core.vocabulary.Namespace.ApiContract
import amf.core.vocabulary.ValueType
import amf.plugins.domain.webapi.models.Request

/**
  * Request metaModel.
  */
object RequestModel extends DomainElementModel with DescriptionField with ParametersFieldModel {

  val Required = Field(Bool, ApiContract + "required", ModelDoc(ModelVocabularies.ApiContract, "required", ""))

  val Payloads = Field(Array(PayloadModel),
                       ApiContract + "payload",
                       ModelDoc(ModelVocabularies.ApiContract, "payload", "Payload for the request"))

  val CookieParameters =
    Field(Array(ParameterModel), ApiContract + "cookieParameter", ModelDoc(ModelVocabularies.ApiContract, "cookie parameter", ""))

  override val `type`: List[ValueType] = ApiContract + "Request" :: DomainElementModel.`type`

  override def fields: List[Field] =
    List(NameFieldSchema.Name,
         Description,
         Required,
         QueryParameters,
         Headers,
         Payloads,
         QueryString,
         UriParameters,
         CookieParameters) ++ DomainElementModel.fields

  override def modelInstance = Request()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiContract,
    "Request",
    "Request information for an operation"
  )
}
