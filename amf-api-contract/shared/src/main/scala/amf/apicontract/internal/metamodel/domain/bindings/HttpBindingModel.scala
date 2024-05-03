package amf.apicontract.internal.metamodel.domain.bindings

import amf.apicontract.client.scala.model.domain.bindings.http.{
  HttpMessageBinding020,
  HttpMessageBinding030,
  HttpOperationBinding010,
  HttpOperationBinding020
}
import amf.core.client.scala.model.domain.AmfObject
import amf.core.client.scala.vocabulary.Namespace.ApiBinding
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.{Str, Int}
import amf.core.internal.metamodel.domain.{ModelDoc, ModelVocabularies}

trait HttpOperationBindingModel extends OperationBindingModel with BindingVersion with BindingQuery {
  override val `type`: List[ValueType] = ApiBinding + "HttpOperationBinding" :: OperationBindingModel.`type`
  override val key: Field              = Type
  override val doc: ModelDoc           = ModelDoc(ModelVocabularies.ApiBinding, "HttpOperationBinding")

  val Method: Field =
    Field(Str, ApiBinding + "method", ModelDoc(ModelVocabularies.ApiBinding, "method", "Operation binding method"))

  override def fields: List[Field] = List(Method, Query, BindingVersion) ++ OperationBindingModel.fields
}

object HttpOperationBindingModel extends HttpOperationBindingModel {
  override def modelInstance: AmfObject = throw new Exception("HttpOperationBindingModel is an abstract class")
}

object HttpOperationBinding010Model extends HttpOperationBindingModel {
  override val `type`: List[ValueType] = ApiBinding + "HttpOperationBinding010" :: OperationBindingModel.`type`
  override val doc: ModelDoc           = ModelDoc(ModelVocabularies.ApiBinding, "HttpOperationBinding010")

  val OperationType: Field =
    Field(Str, ApiBinding + "operationType", ModelDoc(ModelVocabularies.ApiBinding, "type", "Type of operation"))

  override def fields: List[Field] = List(OperationType, Method, Query, BindingVersion) ++ OperationBindingModel.fields

  override def modelInstance: AmfObject = HttpOperationBinding010()
}

object HttpOperationBinding020Model extends HttpOperationBindingModel {
  override val `type`: List[ValueType]  = ApiBinding + "HttpOperationBinding020" :: OperationBindingModel.`type`
  override val doc: ModelDoc            = ModelDoc(ModelVocabularies.ApiBinding, "HttpOperationBinding020")
  override def modelInstance: AmfObject = HttpOperationBinding020()
}

trait HttpMessageBindingModel extends MessageBindingModel with BindingVersion with BindingHeaders {
  override val key: Field              = Type
  override val doc: ModelDoc           = ModelDoc(ModelVocabularies.ApiBinding, "HttpMessageBinding")
  override val `type`: List[ValueType] = ApiBinding + "HttpMessageBinding" :: MessageBindingModel.`type`
  override def fields: List[Field]     = List(Headers, BindingVersion) ++ MessageBindingModel.fields
}

object HttpMessageBindingModel extends HttpMessageBindingModel {
  override def modelInstance: AmfObject = throw new Exception("HttpMessageBindingModel is an abstract class")
}

object HttpMessageBinding020Model extends HttpMessageBindingModel {
  override val doc: ModelDoc            = ModelDoc(ModelVocabularies.ApiBinding, "HttpMessageBinding020")
  override val `type`: List[ValueType]  = ApiBinding + "HttpMessageBinding020" :: MessageBindingModel.`type`
  override def modelInstance: AmfObject = HttpMessageBinding020()
}

object HttpMessageBinding030Model extends HttpMessageBindingModel {
  override val doc: ModelDoc            = ModelDoc(ModelVocabularies.ApiBinding, "HttpMessageBinding030")
  override val `type`: List[ValueType]  = ApiBinding + "HttpMessageBinding030" :: MessageBindingModel.`type`
  override def modelInstance: AmfObject = HttpMessageBinding030()

  val StatusCode: Field =
    Field(
      Int,
      ApiBinding + "statusCode",
      ModelDoc(ModelVocabularies.ApiBinding, "statusCode", "The HTTP response status code according to RFC 9110.")
    )

  override def fields: List[Field] = StatusCode +: HttpMessageBindingModel.fields
}
