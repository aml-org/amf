package amf.apicontract.internal.metamodel.domain.bindings

import amf.apicontract.client.scala.model.domain.bindings.anypointmq.{
  AnypointMQChannelBinding,
  AnypointMQMessageBinding
}
import amf.core.client.scala.model.domain.AmfObject
import amf.core.client.scala.vocabulary.Namespace.ApiBinding
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.Str
import amf.core.internal.metamodel.domain.{ModelDoc, ModelVocabularies}

object AnypointMQMessageBindingModel extends MessageBindingModel with BindingVersion with BindingHeaders {

  override def modelInstance: AmfObject = AnypointMQMessageBinding()

  override def fields: List[Field] =
    List(Headers, BindingVersion) ++ MessageBindingModel.fields

  override val `type`: List[ValueType] = ApiBinding + "AnypointMQMessageBinding" :: MessageBindingModel.`type`

  override val key: Field = Type

  override val doc: ModelDoc = ModelDoc(ModelVocabularies.ApiBinding, "AnypointMQMessageBinding")
}

object AnypointMQChannelBindingModel extends ChannelBindingModel with BindingVersion {
  val Destination: Field =
    Field(
      Str,
      ApiBinding + "destination",
      ModelDoc(
        ModelVocabularies.ApiBinding,
        "destination",
        "The destination (queue or exchange) name for this channel."
      )
    )

  val DestinationType: Field =
    Field(
      Str,
      ApiBinding + "destinationType",
      ModelDoc(
        ModelVocabularies.ApiBinding,
        "destinationType",
        "The type of destination (either exchange or queue or fifo-queue)."
      )
    )

  override def modelInstance: AmfObject = AnypointMQChannelBinding()

  override def fields: List[Field] = List(Destination, DestinationType, BindingVersion) ++ ChannelBindingModel.fields

  override val `type`: List[ValueType] = ApiBinding + "AnypointMQChannelBinding" :: ChannelBindingModel.`type`

  override val key: Field = Type

  override val doc: ModelDoc = ModelDoc(ModelVocabularies.ApiBinding, "AnypointMQChannelBinding")
}
