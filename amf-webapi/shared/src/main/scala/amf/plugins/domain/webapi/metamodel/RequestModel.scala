package amf.plugins.domain.webapi.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Array, Bool, Str}
import amf.core.metamodel.domain.common.DescriptionField
import amf.core.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies, ShapeModel}
import amf.core.vocabulary.Namespace.{Http, Schema}
import amf.core.vocabulary.ValueType
import amf.plugins.domain.webapi.models.Request

/**
  * Request metaModel.
  */
object RequestModel extends DomainElementModel with DescriptionField with ParametersFieldModel {

  val Required = Field(Bool, Http + "required", ModelDoc(ModelVocabularies.Http, "required", ""))

  val Payloads = Field(Array(PayloadModel),
                       Http + "payload",
                       ModelDoc(ModelVocabularies.Http, "payload", "Payload for the request"))

  val CookieParameters =
    Field(Array(ParameterModel), Http + "cookieParameter", ModelDoc(ModelVocabularies.Http, "cookie parameter", ""))

  override val `type`: List[ValueType] = Http + "Request" :: DomainElementModel.`type`

  override def fields: List[Field] =
    List(Description, Required, QueryParameters, Headers, Payloads, QueryString, UriParameters, CookieParameters) ++ DomainElementModel.fields

  override def modelInstance = Request()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Http,
    "Request",
    "Request information for an operation"
  )
}
