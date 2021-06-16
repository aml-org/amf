package amf.apicontract.internal.metamodel.domain.bindings

import amf.core.client.scala.model.domain.AmfObject
import amf.core.client.scala.vocabulary.Namespace.ApiBinding
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.Str
import amf.core.internal.metamodel.domain.{ModelDoc, ModelVocabularies}
import amf.apicontract.client.scala.model.domain.bindings.http.{HttpMessageBinding, HttpOperationBinding}

object HttpOperationBindingModel extends OperationBindingModel with BindingVersion with BindingQuery {

  val OperationType =
    Field(Str, ApiBinding + "operationType", ModelDoc(ModelVocabularies.ApiBinding, "type", "Type of operation"))

  val Method =
    Field(Str, ApiBinding + "method", ModelDoc(ModelVocabularies.ApiBinding, "method", "Operation binding method"))

  override def modelInstance: AmfObject = HttpOperationBinding()

  override def fields: List[Field] = List(OperationType, Method, Query, BindingVersion) ++ OperationBindingModel.fields

  override val `type`: List[ValueType] = ApiBinding + "HttpOperationBinding" :: OperationBindingModel.`type`

  override val key: Field = Type

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiBinding,
    "HttpOperationBinding",
    ""
  )
}

object HttpMessageBindingModel extends MessageBindingModel with BindingVersion with BindingHeaders {

  override def modelInstance: AmfObject = HttpMessageBinding()

  override def fields: List[Field] = List(Headers, BindingVersion) ++ MessageBindingModel.fields

  override val `type`: List[ValueType] = ApiBinding + "HttpMessageBinding" :: MessageBindingModel.`type`

  override val key: Field = Type

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiBinding,
    "HttpMessageBinding",
    ""
  )
}
