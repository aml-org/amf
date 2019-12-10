package amf.plugins.domain.webapi.metamodel.bindings

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Int, Str, Array, Bool}
import amf.core.metamodel.domain.common.NameFieldSchema
import amf.core.metamodel.domain.{ModelDoc, ModelVocabularies, DomainElementModel}
import amf.core.model.domain.AmfObject
import amf.core.vocabulary.Namespace.ApiBinding
import amf.core.vocabulary.ValueType
import amf.plugins.domain.webapi.models.bindings.amqp.{Amqp091OperationBinding, Amqp091ChannelBinding, Amqp091ChannelExchange, Amqp091MessageBinding, Amqp091Queue}

object Amqp091ChannelBindingModel extends ChannelBindingModel with BindingVersion {

  val Is =
    Field(Str, ApiBinding + "is", ModelDoc(ModelVocabularies.ApiBinding, "is", "Defines what type of channel is it"))

  val Exchange = Field(Amqp091ChannelExchangeModel,
                       ApiBinding + "exchange",
                       ModelDoc(ModelVocabularies.ApiBinding, "exchange", "Defines the exchange properties"))

  val Queue = Field(Amqp091QueueModel,
                    ApiBinding + "queue",
                    ModelDoc(ModelVocabularies.ApiBinding, "queue", "Defines the queue properties"))

  override def modelInstance: AmfObject = Amqp091ChannelBinding()

  override def fields: List[Field] = List(Is, Exchange, Queue, BindingVersion) ++ ChannelBindingModel.fields

  override val `type`: List[ValueType] = ApiBinding + "Amqp091ChannelBinding" :: ChannelBindingModel.`type`
}

object Amqp091ChannelExchangeModel extends DomainElementModel with NameFieldSchema {
  val Type =
    Field(Str, ApiBinding + "type", ModelDoc(ModelVocabularies.ApiBinding, "type", "The type of the exchange"))

  val Durable = Field(
    Bool,
    ApiBinding + "durable",
    ModelDoc(ModelVocabularies.ApiBinding, "durable", "Whether the exchange should survive broker restarts or not"))

  val AutoDelete = Field(
    Bool,
    ApiBinding + "autoDelete",
    ModelDoc(ModelVocabularies.ApiBinding,
             "autoDelete",
             "Whether the exchange should be deleted when the last queue is unbound from it")
  )

  val VHost = Field(Str,
                    ApiBinding + "vhost",
                    ModelDoc(ModelVocabularies.ApiBinding, "vhost", "The virtual host of the exchange"))

  override def fields: List[Field] = List(Name, Type, Durable, AutoDelete, VHost) ++ DomainElementModel.fields

  override val `type`: List[ValueType] = ApiBinding + "Amqp091ChannelExchange" :: DomainElementModel.`type`

  override def modelInstance: AmfObject = Amqp091ChannelExchange()
}

object Amqp091QueueModel extends DomainElementModel with NameFieldSchema {
  val Durable = Field(
    Bool,
    ApiBinding + "durable",
    ModelDoc(ModelVocabularies.ApiBinding, "durable", "Whether the exchange should survive broker restarts or not"))

  val Exclusive = Field(Bool,
                        ApiBinding + "exclusive",
                        ModelDoc(ModelVocabularies.ApiBinding,
                                 "exclusive",
                                 "Whether the queue should be used only by one connection or not"))

  val AutoDelete = Field(
    Bool,
    ApiBinding + "autoDelete",
    ModelDoc(ModelVocabularies.ApiBinding,
             "autoDelete",
             "Whether the exchange should be deleted when the last queue is unbound from it")
  )

  val VHost = Field(Str,
                    ApiBinding + "vhost",
                    ModelDoc(ModelVocabularies.ApiBinding, "vhost", "The virtual host of the exchange"))

  override def fields: List[Field] = List(Name, Durable, Exclusive, AutoDelete, VHost) ++ DomainElementModel.fields

  override val `type`: List[ValueType] = ApiBinding + "Amqp091ChannelQueue" :: DomainElementModel.`type`

  override def modelInstance: AmfObject = Amqp091Queue()
}

object Amqp091OperationBindingModel extends OperationBindingModel with BindingVersion {
  val Expiration =
    Field(Int,
          ApiBinding + "expiration",
          ModelDoc(ModelVocabularies.ApiBinding, "expiration", "TTL (Time-To-Live) for the message"))

  val UserId =
    Field(Str,
          ApiBinding + "userId",
          ModelDoc(ModelVocabularies.ApiBinding, "userId", "Identifies the user who has sent the message"))

  val CC =
    Field(Array(Str),
          ApiBinding + "cc",
          ModelDoc(ModelVocabularies.ApiBinding,
                   "cc",
                   "The routing keys the message should be routed to at the time of publishing"))

  val Priority =
    Field(Int,
          ApiBinding + "priority",
          ModelDoc(ModelVocabularies.ApiBinding, "priority", "A priority for the message"))

  val DeliveryMode =
    Field(Int,
          ApiBinding + "deliveryMode",
          ModelDoc(ModelVocabularies.ApiBinding, "deliveryMode", "Delivery mode of the message"))

  val Mandatory =
    Field(Bool,
          ApiBinding + "mandatory",
          ModelDoc(ModelVocabularies.ApiBinding, "mandatory", "Whether the message is mandatory or not"))

  val BCC =
    Field(Array(Str),
          ApiBinding + "bcc",
          ModelDoc(ModelVocabularies.ApiBinding, "bcc", "Like cc but consumers will not receive this information"))

  val ReplyTo =
    Field(Str,
          ApiBinding + "replyTo",
          ModelDoc(ModelVocabularies.ApiBinding,
                   "replyTo",
                   "Name of the queue where the consumer should send the response"))

  val Timestamp =
    Field(Bool,
          ApiBinding + "timestamp",
          ModelDoc(ModelVocabularies.ApiBinding, "timestamp", "Whether the message should include a timestamp or not"))

  val Ack =
    Field(Bool,
          ApiBinding + "ack",
          ModelDoc(ModelVocabularies.ApiBinding, "ack", "Whether the consumer should ack the message or not"))

  override def modelInstance: AmfObject = Amqp091OperationBinding()

  override def fields: List[Field] =
    List(Expiration, UserId, CC, Priority, DeliveryMode, Mandatory, BCC, ReplyTo, Timestamp, Ack, BindingVersion) ++ OperationBindingModel.fields

  override val `type`: List[ValueType] = ApiBinding + "Amqp091OperationBinding" :: OperationBindingModel.`type`
}

object Amqp091MessageBindingModel extends MessageBindingModel with BindingVersion {
  val ContentEncoding =
    Field(Str,
          ApiBinding + "contentEncoding",
          ModelDoc(ModelVocabularies.ApiBinding, "contentEncoding", "MIME encoding for the message content"))

  val MessageType =
    Field(Str,
          ApiBinding + "messageType",
          ModelDoc(ModelVocabularies.ApiBinding, "messageType", "Application-specific message type"))

  override def modelInstance: AmfObject = Amqp091MessageBinding()

  override def fields: List[Field] = List(ContentEncoding, MessageType, BindingVersion) ++ MessageBindingModel.fields

  override val `type`: List[ValueType] = ApiBinding + "Amqp091MessageBinding" :: MessageBindingModel.`type`
}
