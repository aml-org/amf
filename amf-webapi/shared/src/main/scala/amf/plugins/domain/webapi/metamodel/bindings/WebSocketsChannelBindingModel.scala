package amf.plugins.domain.webapi.metamodel.bindings

import amf.core.metamodel.Field
import amf.core.metamodel.Type.Str
import amf.core.metamodel.domain.{ModelDoc, ShapeModel, ModelVocabularies}
import amf.core.model.domain.AmfObject
import amf.core.vocabulary.Namespace.ApiBinding
import amf.core.vocabulary.ValueType
import amf.plugins.domain.webapi.models.bindings.websockets.WebSocketsChannelBinding

object WebSocketsChannelBindingModel extends ChannelBindingModel with BindingVersion {
  val Method =
    Field(Str,
          ApiBinding + "method",
          ModelDoc(ModelVocabularies.ApiBinding, "method", "The HTTP method to use when establishing the connection"))

  val Query = Field(ShapeModel,
                    ApiBinding + "query",
                    ModelDoc(ModelVocabularies.ApiBinding,
                             "query",
                             "A Schema object containing the definitions for each query parameter"))

  val Headers = Field(
    ShapeModel,
    ApiBinding + "headers",
    ModelDoc(ModelVocabularies.ApiBinding,
             "query",
             "A Schema object containing the definitions for each query parameter")
  )

  override def modelInstance: AmfObject = WebSocketsChannelBinding()

  override def fields: List[Field] = List(Method, Query, Headers, BindingVersion) ++ ChannelBindingModel.fields

  override val key: Field = Type

  override val `type`: List[ValueType] = ApiBinding + "WebSocketsChannelBinding" :: ChannelBindingModel.`type`

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiBinding,
    "WebSocketsChannelBinding",
    ""
  )
}
