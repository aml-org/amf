package amf.plugins.domain.webapi.metamodel.bindings

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Bool, Str, Int}
import amf.core.metamodel.domain.{ModelDoc, ModelVocabularies, DomainElementModel}
import amf.core.model.domain.AmfObject
import amf.core.vocabulary.Namespace.ApiBinding
import amf.core.vocabulary.ValueType
import amf.plugins.domain.webapi.metamodel.bindings.Amqp091MessageBindingModel.{
  BindingVersion,
  ContentEncoding,
  MessageType
}
import amf.plugins.domain.webapi.models.bindings.mqtt.{
  MqttOperationBinding,
  MqttServerBinding,
  MqttServerLastWill,
  MqttMessageBinding
}

object MqttServerBindingModel extends ServerBindingModel with BindingVersion {
  val ClientId =
    Field(Str, ApiBinding + "clientId", ModelDoc(ModelVocabularies.ApiBinding, "clientId", "The client identifier"))

  val CleanSession =
    Field(Bool,
          ApiBinding + "cleanSession",
          ModelDoc(ModelVocabularies.ApiBinding, "cleanSession", "Whether to create a persistent connection or not"))

  val LastWill = Field(MqttServerLastWillModel,
                       ApiBinding + "lastWill",
                       ModelDoc(ModelVocabularies.ApiBinding, "lastWill", "Last Will and Testament configuration"))

  val KeepAlive = Field(
    Int,
    ApiBinding + "keepAlive",
    ModelDoc(
      ModelVocabularies.ApiBinding,
      "keepAlive",
      "Interval in seconds of the longest period of time the broker and the client can endure without sending a message"
    )
  )

  override def modelInstance: AmfObject = MqttServerBinding()

  override def fields: List[Field] =
    List(ClientId, CleanSession, LastWill, KeepAlive, BindingVersion) ++ ServerBindingModel.fields

  override val `type`: List[ValueType] = ApiBinding + "MqttServerBinding" :: ServerBindingModel.`type`

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiBinding,
    "MqttServerBinding",
    ""
  )
}

object MqttServerLastWillModel extends DomainElementModel {
  val Topic =
    Field(Str,
          ApiBinding + "topic",
          ModelDoc(ModelVocabularies.ApiBinding,
                   "topic",
                   "The topic where the Last Will and Testament message will be sent"))

  val Qos = Field(
    Int,
    ApiBinding + "qos",
    ModelDoc(
      ModelVocabularies.ApiBinding,
      "qos",
      "Defines how hard the broker/client will try to ensure that the Last Will and Testament message is received")
  )

  val Retain = Field(
    Bool,
    ApiBinding + "retain",
    ModelDoc(ModelVocabularies.ApiBinding,
             "retain",
             "Whether the broker should retain the Last Will and Testament message or not")
  )

  override def fields: List[Field] = List(Topic, Qos, Retain) ++ DomainElementModel.fields

  override val `type`: List[ValueType] = ApiBinding + "MqttServerLastWill" :: DomainElementModel.`type`

  override def modelInstance: AmfObject = MqttServerLastWill()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiBinding,
    "MqttServerLastWill",
    ""
  )
}

object MqttOperationBindingModel extends OperationBindingModel with BindingVersion {
  val Qos =
    Field(
      Int,
      ApiBinding + "qos",
      ModelDoc(ModelVocabularies.ApiBinding,
               "qos",
               "Defines how hard the broker/client will try to ensure that a message is received")
    )

  val Retain =
    Field(Bool,
          ApiBinding + "retain",
          ModelDoc(ModelVocabularies.ApiBinding, "retain", "Whether the broker should retain the message or not"))

  override def modelInstance: AmfObject = MqttOperationBinding()

  override def fields: List[Field] = List(Qos, Retain, BindingVersion) ++ OperationBindingModel.fields

  override val `type`: List[ValueType] = ApiBinding + "MqttOperationBinding" :: OperationBindingModel.`type`

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiBinding,
    "MqttOperationBinding",
    ""
  )
}

object MqttMessageBindingModel extends MessageBindingModel with BindingVersion {
  override def modelInstance: AmfObject = MqttMessageBinding()

  override def fields: List[Field] = List(BindingVersion) ++ MessageBindingModel.fields

  override val `type`: List[ValueType] = ApiBinding + "MqttMessageBinding" :: MessageBindingModel.`type`

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiBinding,
    "MqttMessageBinding",
    ""
  )
}
