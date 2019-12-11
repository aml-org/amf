package amf.plugins.domain.webapi.metamodel.bindings

import amf.core.metamodel.Field
import amf.core.metamodel.Type.Str
import amf.core.metamodel.domain.{ModelDoc, ShapeModel, ModelVocabularies}
import amf.core.model.domain.AmfObject
import amf.core.vocabulary.Namespace.ApiBinding
import amf.core.vocabulary.ValueType
import amf.plugins.domain.webapi.models.bindings.http.{HttpOperationBinding, HttpMessageBinding}

object HttpOperationBindingModel extends OperationBindingModel with BindingVersion {
  val Type =
    Field(Str, ApiBinding + "type", ModelDoc(ModelVocabularies.ApiBinding, "type", "Type of operation"))

  val Method =
    Field(Str, ApiBinding + "method", ModelDoc(ModelVocabularies.ApiBinding, "method", "Operation binding method"))

  val Query =
    Field(ShapeModel,
          ApiBinding + "query",
          ModelDoc(ModelVocabularies.ApiBinding,
                   "query",
                   "A Schema object containing the definitions for each query parameter"))

  override def modelInstance: AmfObject = HttpOperationBinding()

  override def fields: List[Field] = List(Type, Method, Query, BindingVersion) ++ OperationBindingModel.fields

  override val `type`: List[ValueType] = ApiBinding + "HttpOperationBinding" :: OperationBindingModel.`type`

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiBinding,
    "HttpOperationBinding",
    ""
  )
}

object HttpMessageBindingModel extends MessageBindingModel with BindingVersion {
  val Headers =
    Field(
      ShapeModel,
      ApiBinding + "headers",
      ModelDoc(ModelVocabularies.ApiBinding,
               "headers",
               "A Schema object containing the definitions for HTTP-specific headers")
    )

  override def modelInstance: AmfObject = HttpMessageBinding()

  override def fields: List[Field] = List(Headers, BindingVersion) ++ MessageBindingModel.fields

  override val `type`: List[ValueType] = ApiBinding + "HttpMessageBinding" :: MessageBindingModel.`type`

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiBinding,
    "HttpMessageBinding",
    ""
  )
}
