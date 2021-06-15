package amf.plugins.domain.apicontract.metamodel

import amf.core.client.scala.vocabulary.Namespace.ApiContract
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}
import amf.core.internal.metamodel.domain.templates.KeyField
import amf.plugins.domain.apicontract.models.Request
import amf.core.internal.metamodel.Type.{Array, Bool}

/**
  * Request metaModel.
  */
object RequestModel extends DomainElementModel with ParametersFieldModel with MessageModel with KeyField {

  val Required = Field(Bool,
                       ApiContract + "required",
                       ModelDoc(ModelVocabularies.ApiContract, "required", "Marks the parameter as required"))

  val CookieParameters =
    Field(Array(ParameterModel),
          ApiContract + "cookieParameter",
          ModelDoc(ModelVocabularies.ApiContract, "cookieParameter", ""))

  override val `type`: List[ValueType] = ApiContract + "Request" :: MessageModel.`type`

  override def fields: List[Field] =
    List(Required, QueryParameters, QueryString, UriParameters, CookieParameters) ++ MessageModel.fields

  override def modelInstance = Request()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiContract,
    "Request",
    "Request information for an operation"
  )
  override val key: Field = Name
}
