package amf.plugins.domain.webapi.metamodel.bindings

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Str, Bool, Int, Array}
import amf.core.metamodel.domain.common.NameFieldSchema
import amf.core.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}
import amf.core.model.domain.AmfObject
import amf.core.vocabulary.Namespace.ApiContract
import amf.core.vocabulary.ValueType

object Amqp091ChannelBindingModel extends ChannelBindingModel with BindingVersion {

  val Is =
    Field(Str, ApiContract + "is", ModelDoc(ModelVocabularies.ApiContract, "is", "Defines what type of channel is it"))

  val Exchange = Field(Amqp091ChannelExchangeModel,
                       ApiContract + "exchange",
                       ModelDoc(ModelVocabularies.ApiContract, "exchange", "Defines the exchange properties"))

  val Queue = Field(Amqp091QueueExchangeModel,
                    ApiContract + "queue",
                    ModelDoc(ModelVocabularies.ApiContract, "queue", "Defines the queue properties"))

  override def modelInstance: AmfObject = ???

  override def fields: List[Field] = List(Is, Exchange, Queue, BindingVersion) ++ ChannelBindingModel.fields

  override val `type`: List[ValueType] = ApiContract + "Amqp091ChannelBinding" :: ChannelBindingModel.`type`
}

object Amqp091ChannelExchangeModel extends DomainElementModel with NameFieldSchema {
  val Type =
    Field(Str, ApiContract + "type", ModelDoc(ModelVocabularies.ApiContract, "type", "The type of the exchange"))

  val Durable = Field(
    Bool,
    ApiContract + "durable",
    ModelDoc(ModelVocabularies.ApiContract, "durable", "Whether the exchange should survive broker restarts or not"))

  val AutoDelete = Field(
    Bool,
    ApiContract + "autoDelete",
    ModelDoc(ModelVocabularies.ApiContract,
             "autoDelete",
             "Whether the exchange should be deleted when the last queue is unbound from it")
  )

  val VHost = Field(Str,
                    ApiContract + "vhost",
                    ModelDoc(ModelVocabularies.ApiContract, "vhost", "The virtual host of the exchange"))

  override def fields: List[Field] = List(Name, Type, Durable, AutoDelete, VHost) ++ DomainElementModel.fields

  override val `type`: List[ValueType] = ApiContract + "Amqp091ChannelExchange" :: DomainElementModel.`type`

  override def modelInstance: AmfObject = ???
}

object Amqp091QueueExchangeModel extends DomainElementModel with NameFieldSchema {
  val Durable = Field(
    Bool,
    ApiContract + "durable",
    ModelDoc(ModelVocabularies.ApiContract, "durable", "Whether the exchange should survive broker restarts or not"))

  val Exclusive = Field(Bool,
                        ApiContract + "exclusive",
                        ModelDoc(ModelVocabularies.ApiContract,
                                 "exclusive",
                                 "Whether the queue should be used only by one connection or not"))

  val AutoDelete = Field(
    Bool,
    ApiContract + "autoDelete",
    ModelDoc(ModelVocabularies.ApiContract,
             "autoDelete",
             "Whether the exchange should be deleted when the last queue is unbound from it")
  )

  val VHost = Field(Str,
                    ApiContract + "vhost",
                    ModelDoc(ModelVocabularies.ApiContract, "vhost", "The virtual host of the exchange"))

  override def fields: List[Field] = List(Name, Durable, Exclusive, AutoDelete, VHost) ++ DomainElementModel.fields

  override val `type`: List[ValueType] = ApiContract + "Amqp091ChannelQueue" :: DomainElementModel.`type`

  override def modelInstance: AmfObject = ???
}

object Amqp091OperationBindingModel extends OperationBindingModel with BindingVersion {
  val Expiration =
    Field(Int,
          ApiContract + "expiration",
          ModelDoc(ModelVocabularies.ApiContract, "expiration", "TTL (Time-To-Live) for the message"))

  val UserId =
    Field(Str,
          ApiContract + "userId",
          ModelDoc(ModelVocabularies.ApiContract, "userId", "Identifies the user who has sent the message"))

  val CC =
    Field(Array(Str),
          ApiContract + "cc",
          ModelDoc(ModelVocabularies.ApiContract,
                   "cc",
                   "The routing keys the message should be routed to at the time of publishing"))

  val Priority =
    Field(Int,
          ApiContract + "priority",
          ModelDoc(ModelVocabularies.ApiContract, "priority", "A priority for the message"))

  val DeliveryMode =
    Field(Int,
          ApiContract + "deliveryMode",
          ModelDoc(ModelVocabularies.ApiContract, "deliveryMode", "Delivery mode of the message"))

  val Mandatory =
    Field(Bool,
          ApiContract + "mandatory",
          ModelDoc(ModelVocabularies.ApiContract, "mandatory", "Whether the message is mandatory or not"))

  val BCC =
    Field(Array(Str),
          ApiContract + "bcc",
          ModelDoc(ModelVocabularies.ApiContract, "bcc", "Like cc but consumers will not receive this information"))

  val ReplyTo =
    Field(Str,
          ApiContract + "replyTo",
          ModelDoc(ModelVocabularies.ApiContract,
                   "replyTo",
                   "Name of the queue where the consumer should send the response"))

  val Timestamp =
    Field(
      Bool,
      ApiContract + "timestamp",
      ModelDoc(ModelVocabularies.ApiContract, "timestamp", "Whether the message should include a timestamp or not"))

  val Ack =
    Field(Bool,
          ApiContract + "ack",
          ModelDoc(ModelVocabularies.ApiContract, "ack", "Whether the consumer should ack the message or not"))

  override def modelInstance: AmfObject = ???

  override def fields: List[Field] =
    List(Expiration, UserId, CC, Priority, DeliveryMode, Mandatory, BCC, ReplyTo, Timestamp, Ack, BindingVersion) ++ OperationBindingModel.fields

  override val `type`: List[ValueType] = ApiContract + "Amqp091OperationBinding" :: OperationBindingModel.`type`
}

object Amqp091MessageBindingModel extends MessageBindingModel with BindingVersion {
  val ContentEncoding =
    Field(Str,
          ApiContract + "contentEncoding",
          ModelDoc(ModelVocabularies.ApiContract, "contentEncoding", "MIME encoding for the message content"))

  val MessageType =
    Field(Str,
          ApiContract + "messageType",
          ModelDoc(ModelVocabularies.ApiContract, "messageType", "Application-specific message type"))

  override def modelInstance: AmfObject = ???

  override def fields: List[Field] = List(ContentEncoding, MessageType, BindingVersion) ++ MessageBindingModel.fields

  override val `type`: List[ValueType] = ApiContract + "Amqp091MessageBinding" :: MessageBindingModel.`type`
}
