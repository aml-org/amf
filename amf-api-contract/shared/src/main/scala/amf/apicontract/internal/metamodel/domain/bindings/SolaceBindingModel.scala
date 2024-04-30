package amf.apicontract.internal.metamodel.domain.bindings

import amf.apicontract.client.scala.model.domain.bindings.solace.{SolaceOperationBinding, SolaceOperationDestination, SolaceOperationDestination010, SolaceOperationDestination020, SolaceOperationQueue, SolaceOperationTopic, SolaceServerBinding}
import amf.core.client.scala.model.domain.AmfObject
import amf.core.client.scala.vocabulary.Namespace.ApiBinding
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.{Array, Str}
import amf.core.internal.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}
import amf.core.internal.metamodel.domain.common.NameFieldSchema

object SolaceServerBindingModel extends ServerBindingModel with BindingVersion {
  val MsgVpn: Field =
    Field(
      Str,
      ApiBinding + "msgVpn",
      ModelDoc(ModelVocabularies.ApiBinding, "msgVpn", "The Virtual Private Network name on the Solace broker.")
    )

  override def modelInstance: AmfObject = SolaceServerBinding()
  override def fields: List[Field]      = List(MsgVpn, BindingVersion) ++ ServerBindingModel.fields

  override val `type`: List[ValueType] = ApiBinding + "SolaceServerBinding" :: ServerBindingModel.`type`

  override val key: Field = Type

  override val doc: ModelDoc = ModelDoc(ModelVocabularies.ApiBinding, "SolaceServerBinding")
}
object SolaceOperationBindingModel extends OperationBindingModel with BindingVersion {
  val Destinations: Field =
    Field(
      Array(SolaceOperationDestinationModel),
      ApiBinding + "destinations",
      ModelDoc(
        ModelVocabularies.ApiBinding,
        "destinations"
      )
    )

  override def modelInstance: AmfObject = SolaceOperationBinding()

  override def fields: List[Field] = List(Destinations, BindingVersion) ++ OperationBindingModel.fields

  override val `type`: List[ValueType] = ApiBinding + "SolaceOperationBinding" :: OperationBindingModel.`type`

  override val key: Field = Type

  override val doc: ModelDoc = ModelDoc(ModelVocabularies.ApiBinding, "SolaceOperationBinding")
}

trait SolaceOperationDestinationModel extends DomainElementModel {

  val DestinationType: Field = Field(
    Str,
    ApiBinding + "destinationType",
    ModelDoc(
      ModelVocabularies.ApiBinding,
      "destinationType",
      "'queue' or 'topic'. If the type is queue, then the subscriber can bind to the queue, which in turn will subscribe to the topic as represented by the channel name or to the provided topicSubscriptions."
    )
  )

  val DeliveryMode: Field = Field(
    Str,
    ApiBinding + "deliveryMode",
    ModelDoc(
      ModelVocabularies.ApiBinding,
      "deliveryMode",
      "'direct' or 'persistent'. This determines the quality of service for publishing messages. Default is 'persistent'."
    )
  )

  val Queue: Field =
    Field(
      SolaceOperationQueueModel,
      ApiBinding + "queue",
      ModelDoc(
        ModelVocabularies.ApiBinding,
        "queue",
        "Defines the properties of a queue."
      )
    )

  override def fields: List[Field] = List(DestinationType, DeliveryMode, Queue) ++ DomainElementModel.fields

  override val `type`: List[ValueType] = ApiBinding + "SolaceOperationDestination" :: DomainElementModel.`type`

  override val doc: ModelDoc = ModelDoc(ModelVocabularies.ApiBinding, "SolaceOperationDestination")
}
object SolaceOperationDestinationModel extends SolaceOperationDestinationModel {
  override def modelInstance: AmfObject = throw new Exception("SolaceOperationDestinationModel is an abstract class")
}
object SolaceOperationDestination010Model extends SolaceOperationDestinationModel {
  override def modelInstance: AmfObject = SolaceOperationDestination010()

  override val `type`: List[ValueType] =
    ApiBinding + "SolaceOperationDestination010" :: SolaceOperationDestinationModel.`type`

  override val doc: ModelDoc = ModelDoc(ModelVocabularies.ApiBinding, "SolaceOperationDestination010")
}
object SolaceOperationDestination020Model extends SolaceOperationDestinationModel {
  val Topic: Field =
    Field(
      SolaceOperationTopicModel,
      ApiBinding + "topic",
      ModelDoc(
        ModelVocabularies.ApiBinding,
        "topic",
        "Defines the properties of a topic."
      )
    )
  override def fields: List[Field]      = List(DestinationType, DeliveryMode, Queue, Topic) ++ DomainElementModel.fields
  override def modelInstance: AmfObject = SolaceOperationDestination020()

  override val `type`: List[ValueType] =
    ApiBinding + "SolaceOperationDestination020" :: SolaceOperationDestinationModel.`type`

  override val doc: ModelDoc = ModelDoc(ModelVocabularies.ApiBinding, "SolaceOperationDestination020")

}

trait SolaceOperationQueueModel extends DomainElementModel with NameFieldSchema {

  val TopicSubscriptions: Field = Field(
    Array(Str),
    ApiBinding + "topicSubscriptions",
    ModelDoc(
      ModelVocabularies.ApiBinding,
      "topicSubscriptions",
      "A list of topics that the queue subscribes to, only applicable when destinationType is 'queue'. If none is given, the queue subscribes to the topic as represented by the channel name."
    )
  )

  val AccessType: Field = Field(
    Str,
    ApiBinding + "accessType",
    ModelDoc(
      ModelVocabularies.ApiBinding,
      "accessType",
      "'exclusive' or 'nonexclusive'. Only applicable when destinationType is 'queue'."
    )
  )

  override def fields: List[Field] =
    List(TopicSubscriptions, AccessType) ++ DomainElementModel.fields

  override val `type`: List[ValueType] = ApiBinding + "SolaceOperationQueue" :: DomainElementModel.`type`

  override val doc: ModelDoc = ModelDoc(ModelVocabularies.ApiBinding, "SolaceOperationQueue")
}
object SolaceOperationQueueModel extends SolaceOperationQueueModel {
  override def modelInstance: AmfObject = throw new Exception("SolaceOperationQueueModel is an abstract class")
}

trait SolaceOperationTopicModel extends DomainElementModel {

  val TopicSubscriptions: Field = Field(
    Array(Str),
    ApiBinding + "topicSubscriptions",
    ModelDoc(
      ModelVocabularies.ApiBinding,
      "topicSubscriptions",
      "A list of topics that the client subscribes to, only applicable when destinationType is 'topic'. If none is given, the client subscribes to the topic as represented by the channel name."
    )
  )

  override def fields: List[Field] = List(TopicSubscriptions) ++ DomainElementModel.fields

  override val `type`: List[ValueType] = ApiBinding + "SolaceOperationTopic" :: DomainElementModel.`type`

  override val doc: ModelDoc = ModelDoc(ModelVocabularies.ApiBinding, "SolaceOperationTopic")
}
object SolaceOperationTopicModel extends SolaceOperationTopicModel {
  override def modelInstance: AmfObject = throw new Exception("SolaceOperationTopicModel is an abstract class")
}
