package amf.apicontract.internal.metamodel.domain.bindings

import amf.apicontract.client.scala.model.domain.bindings.ibmmq.{IBMMQMessageBinding, IBMMQServerBinding}
import amf.core.client.scala.model.domain.AmfObject
import amf.core.client.scala.vocabulary.Namespace.ApiBinding
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.{Bool, Int, Str}
import amf.core.internal.metamodel.domain.{ModelDoc, ModelVocabularies}

object IBMMQMessageBindingModel extends MessageBindingModel with BindingVersion {
  val MessageType: Field =
    Field(
      Str,
      ApiBinding + "messageType",
      ModelDoc(ModelVocabularies.ApiBinding, "type", "The type of the message")
    )

  val Headers: Field =
    Field(
      Str,
      ApiBinding + "headers",
      ModelDoc(
        ModelVocabularies.ApiBinding,
        "headers",
        "Defines the IBM MQ message headers to include with this message. More than one header can be specified as a comma separated list."
      )
    )

  val Description: Field =
    Field(
      Str,
      ApiBinding + "description",
      ModelDoc(
        ModelVocabularies.ApiBinding,
        "description",
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
