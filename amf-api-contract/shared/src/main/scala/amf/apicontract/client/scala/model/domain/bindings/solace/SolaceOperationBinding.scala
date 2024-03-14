package amf.apicontract.client.scala.model.domain.bindings.solace

import amf.apicontract.client.scala.model.domain.bindings.{BindingVersion, OperationBinding}
import amf.apicontract.internal.metamodel.domain.bindings.{
  SolaceOperationBindingModel,
  SolaceOperationDestinationModel,
  SolaceOperationQueueModel,
  SolaceOperationTopicModel
}
import amf.apicontract.internal.metamodel.domain.bindings.SolaceOperationBindingModel._
import amf.apicontract.internal.spec.async.parser.bindings.Bindings.Solace
import amf.core.client.scala.model.domain.{DomainElement, Linkable, NamedDomainElement}
import amf.core.client.scala.model.{BoolField, IntField, StrField}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.shapes.client.scala.model.domain.Key

class SolaceOperationBinding(override val fields: Fields, override val annotations: Annotations)
    extends OperationBinding
    with BindingVersion
    with Key {
  override protected def bindingVersionField: Field   = BindingVersion
  override def meta: SolaceOperationBindingModel.type = SolaceOperationBindingModel

  def destinations: Seq[SolaceOperationDestination] = fields.field(Destinations)

  def withDestinations(destinations: Seq[SolaceOperationDestination]): this.type = setArray(Destinations, destinations)

  override def key: StrField = fields.field(SolaceOperationBindingModel.key)

  override def componentId: String                = s"/$Solace-operation"
  override def linkCopy(): SolaceOperationBinding = SolaceOperationBinding().withId(id)

  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement =
    SolaceOperationBinding.apply
}

object SolaceOperationBinding {

  def apply(): SolaceOperationBinding = apply(Annotations())

  def apply(annotations: Annotations): SolaceOperationBinding = apply(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): SolaceOperationBinding =
    new SolaceOperationBinding(fields, annotations)
}

class SolaceOperationDestination(override val fields: Fields, override val annotations: Annotations)
    extends DomainElement {
  override def meta: SolaceOperationDestinationModel.type = SolaceOperationDestinationModel

  def destinationType: StrField   = fields.field(SolaceOperationDestinationModel.DestinationType)
  def deliveryMode: StrField      = fields.field(SolaceOperationDestinationModel.DeliveryMode)
  def queue: SolaceOperationQueue = fields.field(SolaceOperationDestinationModel.Queue)
  def topic: SolaceOperationTopic = fields.field(SolaceOperationDestinationModel.Topic)

  def withDestinationType(destinationType: String): this.type =
    set(SolaceOperationDestinationModel.DestinationType, destinationType)
  def withDeliveryMode(deliveryMode: String): this.type =
    set(SolaceOperationDestinationModel.DeliveryMode, deliveryMode)
  def withQueue(queue: SolaceOperationQueue): this.type = set(SolaceOperationDestinationModel.Queue, queue)
  def withTopic(topic: SolaceOperationTopic): this.type = set(SolaceOperationDestinationModel.Topic, topic)

  override def componentId: String = s"/$Solace-destination"
}

object SolaceOperationDestination {

  def apply(): SolaceOperationDestination = apply(Annotations())

  def apply(annotations: Annotations): SolaceOperationDestination = apply(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): SolaceOperationDestination =
    new SolaceOperationDestination(fields, annotations)
}

class SolaceOperationQueue(override val fields: Fields, override val annotations: Annotations)
    extends DomainElement
    with NamedDomainElement {
  override def meta: SolaceOperationQueueModel.type = SolaceOperationQueueModel

  override def nameField: Field = SolaceOperationQueueModel.Name

  def topicSubscriptions: Seq[StrField] = fields.field(SolaceOperationQueueModel.TopicSubscriptions)
  def accessType: StrField              = fields.field(SolaceOperationQueueModel.AccessType)
  def maxMsgSpoolSize: StrField         = fields.field(SolaceOperationQueueModel.MaxMsgSpoolSize)
  def maxTtl: StrField                  = fields.field(SolaceOperationQueueModel.MaxTtl)

  def withTopicSubscriptions(topicSubscriptions: Seq[String]): this.type =
    set(SolaceOperationQueueModel.TopicSubscriptions, topicSubscriptions)
  def withAccessType(accessType: String): this.type = set(SolaceOperationQueueModel.AccessType, accessType)
  def withMaxMsgSpoolSize(maxMsgSpoolSize: String): this.type =
    set(SolaceOperationQueueModel.MaxMsgSpoolSize, maxMsgSpoolSize)
  def withMaxTtl(maxTtl: String): this.type = set(SolaceOperationQueueModel.MaxTtl, maxTtl)

  override def componentId: String = s"/$Solace-queue"
}

object SolaceOperationQueue {

  def apply(): SolaceOperationQueue = apply(Annotations())

  def apply(annotations: Annotations): SolaceOperationQueue = apply(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): SolaceOperationQueue =
    new SolaceOperationQueue(fields, annotations)
}

class SolaceOperationTopic(override val fields: Fields, override val annotations: Annotations) extends DomainElement {
  override def meta: SolaceOperationTopicModel.type = SolaceOperationTopicModel

  def topicSubscriptions: Seq[StrField] = fields.field(SolaceOperationQueueModel.TopicSubscriptions)

  def withTopicSubscriptions(topicSubscriptions: Seq[String]): this.type =
    set(SolaceOperationQueueModel.TopicSubscriptions, topicSubscriptions)

  override def componentId: String = s"/$Solace-topic"
}

object SolaceOperationTopic {

  def apply(): SolaceOperationTopic = apply(Annotations())

  def apply(annotations: Annotations): SolaceOperationTopic = apply(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): SolaceOperationTopic =
    new SolaceOperationTopic(fields, annotations)
}
