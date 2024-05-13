package amf.apicontract.internal.metamodel.domain.bindings

import amf.apicontract.client.scala.model.domain.bindings.mqtt.{
  MqttMessageBinding,
  MqttMessageBinding010,
  MqttMessageBinding020,
  MqttOperationBinding010,
  MqttOperationBinding020,
  MqttServerBinding010,
  MqttServerBinding020,
  MqttServerLastWill
}
import amf.core.client.scala.model.domain.AmfObject
import amf.core.client.scala.vocabulary.Namespace.ApiBinding
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.{Bool, Int, Str}
import amf.core.internal.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies, ShapeModel}

trait MqttServerBindingModel extends ServerBindingModel with BindingVersion {
  override val `type`: List[ValueType] = ApiBinding + "MqttServerBinding" :: ServerBindingModel.`type`
  override val key: Field              = Type
  override val doc: ModelDoc           = ModelDoc(ModelVocabularies.ApiBinding, "MqttServerBinding")

  val ClientId: Field =
    Field(Str, ApiBinding + "clientId", ModelDoc(ModelVocabularies.ApiBinding, "clientId", "The client identifier"))

  val CleanSession: Field =
    Field(
      Bool,
      ApiBinding + "cleanSession",
      ModelDoc(ModelVocabularies.ApiBinding, "cleanSession", "Whether to create a persistent connection or not")
    )

  val LastWill: Field = Field(
    MqttServerLastWillModel,
    ApiBinding + "lastWill",
    ModelDoc(ModelVocabularies.ApiBinding, "lastWill", "Last Will and Testament configuration")
  )

  val KeepAlive: Field = Field(
    Int,
    ApiBinding + "keepAlive",
    ModelDoc(
      ModelVocabularies.ApiBinding,
      "keepAlive",
      "Interval in seconds of the longest period of time the broker and the client can endure without sending a message"
    )
  )

  override def fields: List[Field] =
    List(ClientId, CleanSession, LastWill, KeepAlive, BindingVersion) ++ ServerBindingModel.fields
}

object MqttServerBindingModel extends MqttServerBindingModel {
  override def modelInstance: AmfObject = throw new Exception("MqttServerBindingModel is an abstract class")
}

object MqttServerBinding010Model extends MqttServerBindingModel {
  override val `type`: List[ValueType]  = ApiBinding + "MqttServerBinding010" :: ServerBindingModel.`type`
  override val doc: ModelDoc            = ModelDoc(ModelVocabularies.ApiBinding, "MqttServerBinding010")
  override def modelInstance: AmfObject = MqttServerBinding010()
}

object MqttServerBinding020Model extends MqttServerBindingModel {
  override val `type`: List[ValueType]  = ApiBinding + "MqttServerBinding020" :: ServerBindingModel.`type`
  override val doc: ModelDoc            = ModelDoc(ModelVocabularies.ApiBinding, "MqttServerBinding020")
  override def modelInstance: AmfObject = MqttServerBinding020()

  val SessionExpiryInterval: Field = Field(
    Int,
    ApiBinding + "sessionExpiryInterval",
    ModelDoc(
      ModelVocabularies.ApiBinding,
      "sessionExpiryInterval",
      "Interval in seconds or a Schema Object containing the definition of the interval. The broker maintains a session for a disconnected client until this interval expires."
    )
  )

  val MaximumPacketSize: Field = Field(
    Int,
    ApiBinding + "maximumPacketSize",
    ModelDoc(
      ModelVocabularies.ApiBinding,
      "maximumPacketSize",
      "Number of bytes or a Schema Object representing the maximum packet size the client is willing to accept."
    )
  )

  override def fields: List[Field] = List(SessionExpiryInterval, MaximumPacketSize) ++ MqttServerBindingModel.fields
}

object MqttServerLastWillModel extends DomainElementModel {
  override val `type`: List[ValueType]  = ApiBinding + "MqttServerLastWill" :: DomainElementModel.`type`
  override val doc: ModelDoc            = ModelDoc(ModelVocabularies.ApiBinding, "MqttServerLastWill")
  override def modelInstance: AmfObject = MqttServerLastWill()

  val Topic: Field =
    Field(
      Str,
      ApiBinding + "topic",
      ModelDoc(
        ModelVocabularies.ApiBinding,
        "topic",
        "The topic where the Last Will and Testament message will be sent"
      )
    )

  val Qos: Field = Field(
    Int,
    ApiBinding + "qos",
    ModelDoc(
      ModelVocabularies.ApiBinding,
      "qos",
      "Defines how hard the broker/client will try to ensure that the Last Will and Testament message is received"
    )
  )

  val Retain: Field = Field(
    Bool,
    ApiBinding + "retain",
    ModelDoc(
      ModelVocabularies.ApiBinding,
      "retain",
      "Whether the broker should retain the Last Will and Testament message or not"
    )
  )

  val Message: Field = Field(
    Str,
    ApiBinding + "message",
    ModelDoc(
      ModelVocabularies.ApiBinding,
      "message",
      "Message used to notify other clients about an ungracefully disconnected client."
    )
  )

  override def fields: List[Field] = List(Topic, Qos, Retain, Message) ++ DomainElementModel.fields
}

trait MqttOperationBindingModel extends OperationBindingModel with BindingVersion {
  override val `type`: List[ValueType] = ApiBinding + "MqttOperationBinding" :: OperationBindingModel.`type`
  override val doc: ModelDoc           = ModelDoc(ModelVocabularies.ApiBinding, "MqttOperationBinding")

  val Qos: Field =
    Field(
      Int,
      ApiBinding + "qos",
      ModelDoc(
        ModelVocabularies.ApiBinding,
        "qos",
        "Defines how hard the broker/client will try to ensure that a message is received"
      )
    )

  val Retain: Field =
    Field(
      Bool,
      ApiBinding + "retain",
      ModelDoc(ModelVocabularies.ApiBinding, "retain", "Whether the broker should retain the message or not")
    )

  override val key: Field = Type

  override def fields: List[Field] = List(Qos, Retain, BindingVersion) ++ OperationBindingModel.fields
}

object MqttOperationBindingModel extends MqttOperationBindingModel {
  override def modelInstance: AmfObject = throw new Exception("MqttOperationBinding is an abstract class")
}

object MqttOperationBinding010Model extends MqttOperationBindingModel {
  override def modelInstance: AmfObject = MqttOperationBinding010()
  override val `type`: List[ValueType]  = ApiBinding + "MqttOperationBinding010" :: OperationBindingModel.`type`
  override val doc: ModelDoc            = ModelDoc(ModelVocabularies.ApiBinding, "MqttOperationBinding010")
}

object MqttOperationBinding020Model extends MqttOperationBindingModel {
  override def modelInstance: AmfObject = MqttOperationBinding020()
  override val `type`: List[ValueType]  = ApiBinding + "MqttOperationBinding020" :: OperationBindingModel.`type`
  override val doc: ModelDoc            = ModelDoc(ModelVocabularies.ApiBinding, "MqttOperationBinding020")

  val MessageExpiryInterval: Field =
    Field(
      Int,
      ApiBinding + "messageExpiryInterval",
      ModelDoc(
        ModelVocabularies.ApiBinding,
        "messageExpiryInterval",
        "Interval in seconds or a Schema Object containing the definition of the lifetime of the message."
      )
    )

  override def fields: List[Field] = MessageExpiryInterval +: MqttOperationBindingModel.fields
}

trait MqttMessageBindingModel extends MessageBindingModel with BindingVersion {
  override val key: Field              = Type
  override val `type`: List[ValueType] = ApiBinding + "MqttMessageBinding" :: MessageBindingModel.`type`
  override val doc: ModelDoc           = ModelDoc(ModelVocabularies.ApiBinding, "MqttMessageBinding")
  override def fields: List[Field]     = BindingVersion +: MessageBindingModel.fields
}

object MqttMessageBindingModel extends MqttMessageBindingModel {
  override def modelInstance: AmfObject = throw new Exception("MqttMessageBinding is an abstract class")
}

object MqttMessageBinding010Model extends MqttMessageBindingModel {
  override val `type`: List[ValueType]  = ApiBinding + "MqttMessageBinding010" :: MessageBindingModel.`type`
  override val doc: ModelDoc            = ModelDoc(ModelVocabularies.ApiBinding, "MqttMessageBinding010")
  override def modelInstance: AmfObject = MqttMessageBinding010()
}

object MqttMessageBinding020Model extends MqttMessageBindingModel {
  override val `type`: List[ValueType]  = ApiBinding + "MqttMessageBinding020" :: MessageBindingModel.`type`
  override val doc: ModelDoc            = ModelDoc(ModelVocabularies.ApiBinding, "MqttMessageBinding020")
  override def modelInstance: AmfObject = MqttMessageBinding020()

  val PayloadFormatIndicator: Field =
    Field(
      Int,
      ApiBinding + "payloadFormatIndicator",
      ModelDoc(
        ModelVocabularies.ApiBinding,
        "payloadFormatIndicator",
        "Either: 0 (zero): Indicates that the payload is unspecified bytes, or 1: Indicates that the payload is UTF-8 encoded character data."
      )
    )

  val CorrelationData: Field =
    Field(
      ShapeModel,
      ApiBinding + "correlationData",
      ModelDoc(
        ModelVocabularies.ApiBinding,
        "correlationData",
        "Correlation Data is used by the sender of the request message to identify which request the response message is for when it is received."
      )
    )

  val ContentType: Field =
    Field(
      Str,
      ApiBinding + "contentType",
      ModelDoc(
        ModelVocabularies.ApiBinding,
        "contentType",
        "String describing the content type of the message payload. This should not conflict with the contentType field of the associated AsyncAPI Message object."
      )
    )

  val ResponseTopic: Field =
    Field(
      Str,
      ApiBinding + "responseTopic",
      ModelDoc(
        ModelVocabularies.ApiBinding,
        "responseTopic",
        "The topic (channel URI) for a response message."
      )
    )

  override def fields: List[Field] =
    List(PayloadFormatIndicator, CorrelationData, ContentType, ResponseTopic) ++ MqttMessageBindingModel.fields
}
