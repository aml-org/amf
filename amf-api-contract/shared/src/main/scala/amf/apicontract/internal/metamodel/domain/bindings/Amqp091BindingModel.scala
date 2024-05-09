package amf.apicontract.internal.metamodel.domain.bindings

import amf.apicontract.client.scala.model.domain.bindings.amqp._
import amf.core.client.scala.model.domain.AmfObject
import amf.core.client.scala.vocabulary.Namespace.ApiBinding
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.{Array, Bool, Int, Str}
import amf.core.internal.metamodel.domain.common.NameFieldSchema
import amf.core.internal.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}

trait Amqp091ChannelBindingModel extends ChannelBindingModel with BindingVersion {

  val Is: Field =
    Field(Str, ApiBinding + "is", ModelDoc(ModelVocabularies.ApiBinding, "is", "Defines what type of channel is it"))

  val Exchange: Field = Field(
    Amqp091ChannelExchangeModel,
    ApiBinding + "exchange",
    ModelDoc(ModelVocabularies.ApiBinding, "exchange", "Defines the exchange properties")
  )

  val Queue: Field = Field(
    Amqp091QueueModel,
    ApiBinding + "queue",
    ModelDoc(ModelVocabularies.ApiBinding, "queue", "Defines the queue properties")
  )

  override def fields: List[Field] = List(Is, Exchange, Queue, BindingVersion) ++ ChannelBindingModel.fields

  override val `type`: List[ValueType] = ApiBinding + "Amqp091ChannelBinding" :: ChannelBindingModel.`type`

  override val doc: ModelDoc = ModelDoc(ModelVocabularies.ApiBinding, "Amqp091ChannelBinding")

  override val key: Field = Type
}

object Amqp091ChannelBindingModel extends Amqp091ChannelBindingModel {
  override def modelInstance: AmfObject = throw new Exception("Amqp091ChannelBindingModel is an abstract class")
}

object Amqp091ChannelBinding010Model extends Amqp091ChannelBindingModel {
  override val Exchange: Field = Field(
    Amqp091ChannelExchange010Model,
    ApiBinding + "exchange",
    ModelDoc(ModelVocabularies.ApiBinding, "exchange", "Defines the exchange properties")
  )

  override val Queue: Field = Field(
    Amqp091Queue010Model,
    ApiBinding + "queue",
    ModelDoc(ModelVocabularies.ApiBinding, "queue", "Defines the queue properties")
  )

  override def modelInstance: AmfObject = Amqp091ChannelBinding010()

  override val `type`: List[ValueType] = ApiBinding + "Amqp091ChannelBinding010" :: ChannelBindingModel.`type`

  override val doc: ModelDoc = ModelDoc(ModelVocabularies.ApiBinding, "Amqp091ChannelBinding010")
}

object Amqp091ChannelBinding020Model extends Amqp091ChannelBindingModel {
  override val Exchange: Field = Field(
    Amqp091ChannelExchange020Model,
    ApiBinding + "exchange",
    ModelDoc(ModelVocabularies.ApiBinding, "exchange", "Defines the exchange properties")
  )

  override val Queue: Field = Field(
    Amqp091Queue020Model,
    ApiBinding + "queue",
    ModelDoc(ModelVocabularies.ApiBinding, "queue", "Defines the queue properties")
  )

  override def modelInstance: AmfObject = Amqp091ChannelBinding020()

  override val `type`: List[ValueType] = ApiBinding + "Amqp091ChannelBinding020" :: ChannelBindingModel.`type`

  override val doc: ModelDoc = ModelDoc(ModelVocabularies.ApiBinding, "Amqp091ChannelBinding020")
}

trait Amqp091ChannelExchangeModel extends DomainElementModel with NameFieldSchema {
  val Type: Field =
    Field(Str, ApiBinding + "type", ModelDoc(ModelVocabularies.ApiBinding, "type", "The type of the exchange"))

  val Durable: Field = Field(
    Bool,
    ApiBinding + "durable",
    ModelDoc(ModelVocabularies.ApiBinding, "durable", "Whether the exchange should survive broker restarts or not")
  )

  val AutoDelete: Field = Field(
    Bool,
    ApiBinding + "autoDelete",
    ModelDoc(
      ModelVocabularies.ApiBinding,
      "autoDelete",
      "Whether the exchange should be deleted when the last queue is unbound from it"
    )
  )

  override def fields: List[Field] = List(Name, Type, Durable, AutoDelete) ++ DomainElementModel.fields

  override val `type`: List[ValueType] = ApiBinding + "Amqp091ChannelExchange" :: DomainElementModel.`type`

  override val doc: ModelDoc = ModelDoc(ModelVocabularies.ApiBinding, "Amqp091ChannelExchange")
}

object Amqp091ChannelExchangeModel extends Amqp091ChannelExchangeModel {
  override def modelInstance: AmfObject = throw new Exception("Amqp091ChannelExchangeModel is an abstract class")
}

object Amqp091ChannelExchange010Model extends Amqp091ChannelExchangeModel {
  override val `type`: List[ValueType] = ApiBinding + "Amqp091ChannelExchange010" :: DomainElementModel.`type`

  override def modelInstance: AmfObject = Amqp091ChannelExchange010()

  override val doc: ModelDoc = ModelDoc(ModelVocabularies.ApiBinding, "Amqp091ChannelExchange010")
}

object Amqp091ChannelExchange020Model extends Amqp091ChannelExchangeModel {
  val VHost: Field = Field(
    Str,
    ApiBinding + "vhost",
    ModelDoc(ModelVocabularies.ApiBinding, "vhost", "The virtual host of the exchange")
  )

  override def fields: List[Field] = List(Name, Type, Durable, AutoDelete, VHost) ++ DomainElementModel.fields

  override val `type`: List[ValueType] = ApiBinding + "Amqp091ChannelExchange020" :: DomainElementModel.`type`

  override def modelInstance: AmfObject = Amqp091ChannelExchange020()

  override val doc: ModelDoc = ModelDoc(ModelVocabularies.ApiBinding, "Amqp091ChannelExchange020")
}

trait Amqp091QueueModel extends DomainElementModel with NameFieldSchema {
  val Durable: Field = Field(
    Bool,
    ApiBinding + "durable",
    ModelDoc(ModelVocabularies.ApiBinding, "durable", "Whether the exchange should survive broker restarts or not")
  )

  val Exclusive: Field = Field(
    Bool,
    ApiBinding + "exclusive",
    ModelDoc(
      ModelVocabularies.ApiBinding,
      "exclusive",
      "Whether the queue should be used only by one connection or not"
    )
  )

  val AutoDelete: Field = Field(
    Bool,
    ApiBinding + "autoDelete",
    ModelDoc(
      ModelVocabularies.ApiBinding,
      "autoDelete",
      "Whether the exchange should be deleted when the last queue is unbound from it"
    )
  )

  override def fields: List[Field] = List(Name, Durable, Exclusive, AutoDelete) ++ DomainElementModel.fields

  override val `type`: List[ValueType] = ApiBinding + "Amqp091ChannelQueue" :: DomainElementModel.`type`

  override val doc: ModelDoc = ModelDoc(ModelVocabularies.ApiBinding, "Amqp091Queue")
}

object Amqp091QueueModel extends Amqp091QueueModel {
  override def modelInstance: AmfObject = throw new Exception("Amqp091ChannelExchangeModel is an abstract class")
}

object Amqp091Queue010Model extends Amqp091QueueModel {
  override val `type`: List[ValueType] = ApiBinding + "Amqp091ChannelQueue010" :: DomainElementModel.`type`

  override def modelInstance: AmfObject = Amqp091Queue010()

  override val doc: ModelDoc = ModelDoc(ModelVocabularies.ApiBinding, "Amqp091Queue010")
}

object Amqp091Queue020Model extends Amqp091QueueModel {
  val VHost: Field = Field(
    Str,
    ApiBinding + "vhost",
    ModelDoc(ModelVocabularies.ApiBinding, "vhost", "The virtual host of the exchange")
  )

  override def fields: List[Field] = List(Name, Durable, Exclusive, AutoDelete, VHost) ++ DomainElementModel.fields

  override val `type`: List[ValueType] = ApiBinding + "Amqp091ChannelQueue020" :: DomainElementModel.`type`

  override def modelInstance: AmfObject = Amqp091Queue020()

  override val doc: ModelDoc = ModelDoc(ModelVocabularies.ApiBinding, "Amqp091Queue020")
}

trait Amqp091OperationBindingModel extends OperationBindingModel with BindingVersion {
  val Expiration: Field =
    Field(
      Int,
      ApiBinding + "expiration",
      ModelDoc(ModelVocabularies.ApiBinding, "expiration", "TTL (Time-To-Live) for the message")
    )

  val UserId: Field =
    Field(
      Str,
      ApiBinding + "userId",
      ModelDoc(ModelVocabularies.ApiBinding, "userId", "Identifies the user who has sent the message")
    )

  val CC: Field =
    Field(
      Array(Str),
      ApiBinding + "cc",
      ModelDoc(
        ModelVocabularies.ApiBinding,
        "cc",
        "The routing keys the message should be routed to at the time of publishing"
      )
    )

  val Priority: Field =
    Field(
      Int,
      ApiBinding + "priority",
      ModelDoc(ModelVocabularies.ApiBinding, "priority", "A priority for the message")
    )

  val DeliveryMode: Field =
    Field(
      Int,
      ApiBinding + "deliveryMode",
      ModelDoc(ModelVocabularies.ApiBinding, "deliveryMode", "Delivery mode of the message")
    )

  val Mandatory: Field =
    Field(
      Bool,
      ApiBinding + "mandatory",
      ModelDoc(ModelVocabularies.ApiBinding, "mandatory", "Whether the message is mandatory or not")
    )

  val BCC: Field =
    Field(
      Array(Str),
      ApiBinding + "bcc",
      ModelDoc(ModelVocabularies.ApiBinding, "bcc", "Like cc but consumers will not receive this information")
    )

  val Timestamp: Field =
    Field(
      Bool,
      ApiBinding + "timestamp",
      ModelDoc(ModelVocabularies.ApiBinding, "timestamp", "Whether the message should include a timestamp or not")
    )

  val Ack: Field =
    Field(
      Bool,
      ApiBinding + "ack",
      ModelDoc(ModelVocabularies.ApiBinding, "ack", "Whether the consumer should ack the message or not")
    )

  override def fields: List[Field] =
    List(
      Expiration,
      UserId,
      CC,
      Priority,
      DeliveryMode,
      Mandatory,
      BCC,
      Timestamp,
      Ack,
      BindingVersion
    ) ++ OperationBindingModel.fields

  override val `type`: List[ValueType] = ApiBinding + "Amqp091OperationBinding" :: OperationBindingModel.`type`

  override val key: Field = Type

  override val doc: ModelDoc = ModelDoc(ModelVocabularies.ApiBinding, "Amqp091OperationBinding")
}

object Amqp091OperationBindingModel extends Amqp091OperationBindingModel {
  override def modelInstance: AmfObject = throw new Exception("Amqp091OperationBindingModel is an abstract class")
}

// version 0.1.0 and 0.2.0 are the same
object Amqp091OperationBinding010Model extends Amqp091OperationBindingModel {
  override def modelInstance: AmfObject = Amqp091OperationBinding010()

  val ReplyTo: Field =
    Field(
      Str,
      ApiBinding + "replyTo",
      ModelDoc(ModelVocabularies.ApiBinding, "replyTo", "Name of the queue where the consumer should send the response")
    )

  override def fields: List[Field] =
    List(
      Expiration,
      UserId,
      CC,
      Priority,
      DeliveryMode,
      Mandatory,
      BCC,
      ReplyTo,
      Timestamp,
      Ack,
      BindingVersion
    ) ++ OperationBindingModel.fields

  override val `type`: List[ValueType] = ApiBinding + "Amqp091OperationBinding010" :: OperationBindingModel.`type`

  override val doc: ModelDoc = ModelDoc(ModelVocabularies.ApiBinding, "Amqp091OperationBinding010")
}

// version 0.3.0 removes the `replyTo` field, so it's the same as the base type
object Amqp091OperationBinding030Model extends Amqp091OperationBindingModel {
  override def fields: List[Field] =
    List(
      Expiration,
      UserId,
      CC,
      Priority,
      DeliveryMode,
      Mandatory,
      BCC,
      Timestamp,
      Ack,
      BindingVersion
    ) ++ OperationBindingModel.fields

  override def modelInstance: AmfObject = Amqp091OperationBinding030()

  override val `type`: List[ValueType] = ApiBinding + "Amqp091OperationBinding030" :: OperationBindingModel.`type`

  override val doc: ModelDoc = ModelDoc(ModelVocabularies.ApiBinding, "Amqp091OperationBinding030")
}

object Amqp091MessageBindingModel extends MessageBindingModel with BindingVersion {
  val ContentEncoding =
    Field(
      Str,
      ApiBinding + "contentEncoding",
      ModelDoc(ModelVocabularies.ApiBinding, "contentEncoding", "MIME encoding for the message content")
    )

  val MessageType =
    Field(
      Str,
      ApiBinding + "messageType",
      ModelDoc(ModelVocabularies.ApiBinding, "messageType", "Application-specific message type")
    )

  override def modelInstance: AmfObject = Amqp091MessageBinding()

  override def fields: List[Field] = List(ContentEncoding, MessageType, BindingVersion) ++ MessageBindingModel.fields

  override val `type`: List[ValueType] = ApiBinding + "Amqp091MessageBinding" :: MessageBindingModel.`type`

  override val key: Field = Type

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiBinding,
    "Amqp091MessageBinding",
    ""
  )
}
