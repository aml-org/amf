package amf.plugins.domain.webapi.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Array, Bool}
import amf.core.metamodel.domain.templates.KeyField
import amf.core.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}
import amf.core.vocabulary.Namespace.ApiContract
import amf.core.vocabulary.ValueType
import amf.plugins.domain.webapi.models.Request

/**
  * Request metaModel.
  */
object RequestModel extends DomainElementModel with ParametersFieldModel with MessageModel with KeyField {

  val Required = Field(Bool, ApiContract + "required", ModelDoc(ModelVocabularies.ApiContract, "required", ""))

  val CookieParameters =
    Field(Array(ParameterModel),
          ApiContract + "cookieParameter",
          ModelDoc(ModelVocabularies.ApiContract, "cookie parameter", ""))

  override val `type`: List[ValueType] = ApiContract + "Request" :: MessageModel.`type`

  override def fields: List[Field] =
    List(Required, QueryParameters, Headers, QueryString, UriParameters, CookieParameters) ++ MessageModel.fields

  override def modelInstance = Request()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiContract,
    "Request",
    "Request information for an operation"
  )
  override val key: Field = Name
}
