package amf.plugins.domain.webapi.metamodel.bindings

import amf.core.metamodel.Field
import amf.core.metamodel.Type.Str
import amf.core.metamodel.domain.{ModelDoc, ModelVocabularies, ShapeModel}
import amf.core.model.domain.AmfObject
import amf.core.vocabulary.Namespace.ApiContract
import amf.core.vocabulary.ValueType

object HttpOperationBindingModel extends OperationBindingModel with BindingVersion {
  val Type =
    Field(Str, ApiContract + "type", ModelDoc(ModelVocabularies.ApiContract, "type", "Type of operation"))

  val Method =
    Field(Str, ApiContract + "method", ModelDoc(ModelVocabularies.ApiContract, "method", "Operation binding method"))

  val Query =
    Field(ShapeModel,
          ApiContract + "query",
          ModelDoc(ModelVocabularies.ApiContract,
                   "query",
                   "A Schema object containing the definitions for each query parameter"))

  override def modelInstance: AmfObject = ???

  override def fields: List[Field] = List(Type, Method, Query, BindingVersion) ++ OperationBindingModel.fields

  override val `type`: List[ValueType] = ApiContract + "HttpOperationBinding" :: OperationBindingModel.`type`
}

object HttpMessageBindingModel extends OperationBindingModel with BindingVersion {
  val Headers =
    Field(
      ShapeModel,
      ApiContract + "headers",
      ModelDoc(ModelVocabularies.ApiContract,
               "headers",
               "A Schema object containing the definitions for HTTP-specific headers")
    )

  override def modelInstance: AmfObject = ???

  override def fields: List[Field] = List(Headers, BindingVersion) ++ MessageBindingModel.fields

  override val `type`: List[ValueType] = ApiContract + "HttpMessageBinding" :: MessageBindingModel.`type`
}
