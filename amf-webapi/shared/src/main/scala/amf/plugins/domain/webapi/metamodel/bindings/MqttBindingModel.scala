package amf.plugins.domain.webapi.metamodel.bindings

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Bool, Int, Str}
import amf.core.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}
import amf.core.model.domain.AmfObject
import amf.core.vocabulary.Namespace.ApiContract
import amf.core.vocabulary.ValueType
import amf.plugins.domain.webapi.metamodel.bindings.Amqp091MessageBindingModel.{
  BindingVersion,
  ContentEncoding,
  MessageType
}

object MqttServerBindingModel extends ServerBindingModel with BindingVersion {
  val ClientId =
    Field(Str, ApiContract + "clientId", ModelDoc(ModelVocabularies.ApiContract, "clientId", "The client identifier"))

  val ClientSession =
    Field(Bool,
          ApiContract + "cleanSession",
          ModelDoc(ModelVocabularies.ApiContract, "cleanSession", "Whether to create a persistent connection or not"))

  val LastWill = Field(MqttServerLastWillModel,
                       ApiContract + "lastWill",
                       ModelDoc(ModelVocabularies.ApiContract, "lastWill", "Last Will and Testament configuration"))

  val KeepAlive = Field(
    Int,
    ApiContract + "keepAlive",
    ModelDoc(
      ModelVocabularies.ApiContract,
      "keepAlive",
      "Interval in seconds of the longest period of time the broker and the client can endure without sending a message"
    )
  )

  override def modelInstance: AmfObject = ???

  override def fields: List[Field] =
    List(ClientId, ClientSession, LastWill, KeepAlive, BindingVersion) ++ ServerBindingModel.fields

  override val `type`: List[ValueType] = ApiContract + "MqttServerBinding" :: ServerBindingModel.`type`
}

object MqttServerLastWillModel extends DomainElementModel {
  val Topic =
    Field(Str,
          ApiContract + "topic",
          ModelDoc(ModelVocabularies.ApiContract,
                   "topic",
                   "The topic where the Last Will and Testament message will be sent"))

  val Qos = Field(
    Int,
    ApiContract + "qos",
    ModelDoc(
      ModelVocabularies.ApiContract,
      "qos",
      "Defines how hard the broker/client will try to ensure that the Last Will and Testament message is received")
  )

  val Retain = Field(
    Bool,
    ApiContract + "retain",
    ModelDoc(ModelVocabularies.ApiContract,
             "retain",
             "Whether the broker should retain the Last Will and Testament message or not")
  )

  override def fields: List[Field] = List(Topic, Qos, Retain) ++ DomainElementModel.fields

  override val `type`: List[ValueType] = ApiContract + "MqttServerLastWill" :: DomainElementModel.`type`

  override def modelInstance: AmfObject = ???
}

object MqttOperationBindingModel extends OperationBindingModel with BindingVersion {
  val Qos =
    Field(
      Int,
      ApiContract + "qos",
      ModelDoc(ModelVocabularies.ApiContract,
               "qos",
               "Defines how hard the broker/client will try to ensure that a message is received")
    )

  val Retain =
    Field(Bool,
          ApiContract + "retain",
          ModelDoc(ModelVocabularies.ApiContract, "retain", "Whether the broker should retain the message or not"))

  override def modelInstance: AmfObject = ???

  override def fields: List[Field] = List(Qos, Retain, BindingVersion) ++ OperationBindingModel.fields

  override val `type`: List[ValueType] = ApiContract + "MqttOperationBinding" :: OperationBindingModel.`type`
}

object MqttMessageBindingModel extends MessageBindingModel {
  override def modelInstance: AmfObject = ???

  override def fields: List[Field] = List(ContentEncoding, MessageType, BindingVersion) ++ MessageBindingModel.fields

  override val `type`: List[ValueType] = ApiContract + "MqttMessageBinding" :: MessageBindingModel.`type`
}
