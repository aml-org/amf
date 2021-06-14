package amf.plugins.domain.apicontract.metamodel.bindings

import amf.core.client.scala.model.domain.AmfObject
import amf.core.client.scala.vocabulary.Namespace.ApiBinding
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.Str
import amf.core.internal.metamodel.domain.{ModelDoc, ModelVocabularies}
import amf.core.metamodel.Field
import amf.core.metamodel.Type.Str
import amf.core.metamodel.domain.{ModelDoc, ModelVocabularies, ShapeModel}
import amf.core.model.domain.AmfObject
import amf.core.vocabulary.Namespace.ApiBinding
import amf.core.vocabulary.ValueType
import amf.plugins.domain.apicontract.models.bindings.websockets.WebSocketsChannelBinding

object WebSocketsChannelBindingModel
    extends ChannelBindingModel
    with BindingVersion
    with BindingHeaders
    with BindingQuery {
  val Method =
    Field(Str,
          ApiBinding + "method",
          ModelDoc(ModelVocabularies.ApiBinding, "method", "The HTTP method to use when establishing the connection"))

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
