package amf.apicontract.internal.metamodel.domain.bindings

import amf.apicontract.client.scala.model.domain.bindings.ibmmq.{
  IBMMQChannelBinding,
  IBMMQChannelQueue,
  IBMMQChannelTopic,
  IBMMQMessageBinding,
  IBMMQServerBinding
}
import amf.core.client.scala.model.domain.AmfObject
import amf.core.client.scala.vocabulary.Namespace.{ApiBinding, Core}
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.{Bool, Int, Str, Array}
import amf.core.internal.metamodel.domain.common.NameFieldSchema
import amf.core.internal.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}

object IBMMQMessageBindingModel extends MessageBindingModel with BindingVersion {
  val MessageType: Field =
    Field(
      Str,
      ApiBinding + "messageType",
      ModelDoc(ModelVocabularies.ApiBinding, "type", "The type of the message")
    )

  val Headers: Field =
    Field(
      Array(Str),
      ApiBinding + "headers",
      ModelDoc(
        ModelVocabularies.ApiBinding,
        "headers",
        "Defines the IBM MQ message headers to include with this message. More than one header can be specified as a comma separated list."
      )
    )

  val Description: Field = Field(
    Str,
    Core + "description",
    ModelDoc(
      ModelVocabularies.Core,
      "Provides additional information for application developers: describes the message type or format."
    )
  )

  val Expiry: Field =
    Field(
      Int,
      ApiBinding + "expiry",
      ModelDoc(
        ModelVocabularies.ApiBinding,
        "expiry",
        "This is a period of time expressed in milliseconds and set by the application that puts the message."
      )
    )

  override def modelInstance: AmfObject = IBMMQMessageBinding()

  override def fields: List[Field] =
    List(MessageType, Headers, Description, Expiry, BindingVersion) ++ MessageBindingModel.fields

  override val `type`: List[ValueType] = ApiBinding + "IBMMQMessageBinding" :: MessageBindingModel.`type`

  override val key: Field = Type

  override val doc: ModelDoc = ModelDoc(ModelVocabularies.ApiBinding, "IBMMQMessageBinding")
}

object IBMMQServerBindingModel extends ServerBindingModel with BindingVersion {
  val GroupId: Field =
    Field(
      Str,
      ApiBinding + "groupId",
      ModelDoc(
        ModelVocabularies.ApiBinding,
        "groupId",
        "Defines a logical group of IBM MQ server objects. This is necessary to specify multi-endpoint configurations used in high availability deployments. If omitted, the server object is not part of a group."
      )
    )

  val CcdtQueueManagerName: Field =
    Field(
      Str,
      ApiBinding + "ccdtQueueManagerName",
      ModelDoc(
        ModelVocabularies.ApiBinding,
        "ccdtQueueManagerName",
        "The name of the IBM MQ queue manager to bind to in the CCDT file."
      )
    )

  val CipherSpec: Field =
    Field(
      Str,
      ApiBinding + "cipherSpec",
      ModelDoc(
        ModelVocabularies.ApiBinding,
        "cipherSpec",
        "The recommended cipher specification used to establish a TLS connection between the client and the IBM MQ queue manager."
      )
    )

  val MultiEndpointServer: Field =
    Field(
      Bool,
      ApiBinding + "multiEndpointServer",
      ModelDoc(
        ModelVocabularies.ApiBinding,
        "multiEndpointServer",
        "If multiEndpointServer is true then multiple connections can be workload balanced and applications should not make assumptions as to where messages are processed. Where message ordering, or affinity to specific message resources is necessary, a single endpoint (multiEndpointServer = false) may be required."
      )
    )

  val HeartBeatInterval: Field =
    Field(
      Int,
      ApiBinding + "heartBeatInterval",
      ModelDoc(
        ModelVocabularies.ApiBinding,
        "heartBeatInterval",
        "The recommended value (in seconds) for the heartbeat sent to the queue manager during periods of inactivity. A value of zero means that no heart beats are sent. A value of 1 means that the client will use the value defined by the queue manager."
      )
    )

  override def modelInstance: AmfObject = IBMMQServerBinding()

  override def fields: List[Field] =
    List(
      GroupId,
      CcdtQueueManagerName,
      CipherSpec,
      MultiEndpointServer,
      HeartBeatInterval,
      BindingVersion
    ) ++ ServerBindingModel.fields

  override val `type`: List[ValueType] = ApiBinding + "IBMMQServerBinding" :: ServerBindingModel.`type`

  override val key: Field = Type

  override val doc: ModelDoc = ModelDoc(ModelVocabularies.ApiBinding, "IBMMQServerBinding")
}

object IBMMQChannelBindingModel extends ChannelBindingModel with BindingVersion {
  val DestinationType: Field =
    Field(
      Str,
      ApiBinding + "destinationType",
      ModelDoc(
        ModelVocabularies.ApiBinding,
        "destinationType",
        "Defines the type of AsyncAPI channel."
      )
    )

  val Queue: Field =
    Field(
      IBMMQChannelQueueModel,
      ApiBinding + "queue",
      ModelDoc(
        ModelVocabularies.ApiBinding,
        "queue",
        "Defines the properties of a queue."
      )
    )

  val Topic: Field =
    Field(
      IBMMQChannelTopicModel,
      ApiBinding + "topic",
      ModelDoc(
        ModelVocabularies.ApiBinding,
        "topic",
        "Defines the properties of a topic."
      )
    )

  val MaxMsgLength: Field =
    Field(
      Int,
      ApiBinding + "maxMsgLength",
      ModelDoc(
        ModelVocabularies.ApiBinding,
        "maxMsgLength",
        "The maximum length of the physical message (in bytes) accepted by the Topic or Queue. Messages produced that are greater in size than this value may fail to be delivered."
      )
    )

  override def modelInstance: AmfObject = IBMMQChannelBinding()

  override def fields: List[Field] =
    List(
      DestinationType,
      Queue,
      Topic,
      MaxMsgLength,
      BindingVersion
    ) ++ ChannelBindingModel.fields

  override val `type`: List[ValueType] = ApiBinding + "IBMMQChannelBinding" :: ChannelBindingModel.`type`

  override val key: Field = Type

  override val doc: ModelDoc = ModelDoc(ModelVocabularies.ApiBinding, "IBMMQChannelBinding")
}

object IBMMQChannelQueueModel extends DomainElementModel with NameFieldSchema {

  val ObjectName: Field = Field(
    Str,
    ApiBinding + "objectName",
    ModelDoc(
      ModelVocabularies.ApiBinding,
      "objectName",
      "Defines the name of the IBM MQ queue associated with the channel."
    )
  )

  val IsPartitioned: Field = Field(
    Bool,
    ApiBinding + "isPartitioned",
    ModelDoc(
      ModelVocabularies.ApiBinding,
      "isPartitioned",
      "Defines if the queue is a cluster queue and therefore partitioned. If true, a binding option MAY be specified when accessing the queue."
    )
  )

  val Exclusive: Field = Field(
    Bool,
    ApiBinding + "exclusive",
    ModelDoc(ModelVocabularies.ApiBinding, "exclusive", "Specifies if it is recommended to open the queue exclusively.")
  )

  override def fields: List[Field] = List(Name, ObjectName, IsPartitioned, Exclusive) ++ DomainElementModel.fields

  override val `type`: List[ValueType] = ApiBinding + "IBMMQChannelQueue" :: DomainElementModel.`type`

  override def modelInstance: AmfObject = IBMMQChannelQueue()

  override val doc: ModelDoc = ModelDoc(ModelVocabularies.ApiBinding, "IBMMQChannelQueue")
}

object IBMMQChannelTopicModel extends DomainElementModel with NameFieldSchema {

  val String: Field = Field(
    Str,
    ApiBinding + "string",
    ModelDoc(
      ModelVocabularies.ApiBinding,
      "string",
      "The value of the IBM MQ topic string to be used."
    )
  )

  val ObjectName: Field = Field(
    Str,
    ApiBinding + "objectName",
    ModelDoc(
      ModelVocabularies.ApiBinding,
      "objectName",
      "The name of the IBM MQ topic object."
    )
  )

  val DurablePermitted: Field = Field(
    Bool,
    ApiBinding + "durablePermitted",
    ModelDoc(ModelVocabularies.ApiBinding, "durablePermitted", "Defines if the subscription may be durable.")
  )

  val LastMsgRetained: Field = Field(
    Bool,
    ApiBinding + "lastMsgRetained",
    ModelDoc(
      ModelVocabularies.ApiBinding,
      "lastMsgRetained",
      "Defines if the last message published will be made available to new subscriptions."
    )
  )

  override def fields: List[Field] =
    List(Name, String, ObjectName, DurablePermitted, LastMsgRetained) ++ DomainElementModel.fields

  override val `type`: List[ValueType] = ApiBinding + "IBMMQChannelTopic" :: DomainElementModel.`type`

  override def modelInstance: AmfObject = IBMMQChannelTopic()

  override val doc: ModelDoc = ModelDoc(ModelVocabularies.ApiBinding, "IBMMQChannelTopic")
}
