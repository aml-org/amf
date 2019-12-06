package amf.plugins.domain.webapi.metamodel.bindings

import amf.core.metamodel.Field
import amf.core.metamodel.Type.Str
import amf.core.metamodel.domain.{ModelDoc, ModelVocabularies, ShapeModel}
import amf.core.model.domain.AmfObject
import amf.core.vocabulary.Namespace.ApiContract
import amf.core.vocabulary.ValueType

object WebSocketsChannelBindingModel extends ChannelBindingModel with BindingVersion {
  val Method =
    Field(Str,
          ApiContract + "method",
          ModelDoc(ModelVocabularies.ApiContract, "method", "The HTTP method to use when establishing the connection"))

  val Query = Field(ShapeModel,
                    ApiContract + "query",
                    ModelDoc(ModelVocabularies.ApiContract,
                             "query",
                             "A Schema object containing the definitions for each query parameter"))

  val Headers = Field(
    ShapeModel,
    ApiContract + "headers",
    ModelDoc(ModelVocabularies.ApiContract,
             "query",
             "A Schema object containing the definitions for each query parameter")
  )

  override def modelInstance: AmfObject = ???

  override def fields: List[Field] = List(Method, Query, Headers, BindingVersion) ++ ChannelBindingModel.fields

  override val `type`: List[ValueType] = ApiContract + "WebSocketsChannelBinding" :: ChannelBindingModel.`type`
}
