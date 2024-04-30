package amf.apicontract.client.scala.model.domain.bindings.solace

import amf.apicontract.client.scala.model.domain.bindings.{BindingVersion, OperationBinding}
import amf.apicontract.internal.metamodel.domain.bindings.{SolaceOperationBindingModel, SolaceOperationDestination010Model, SolaceOperationDestination020Model, SolaceOperationDestinationModel, SolaceOperationQueueModel, SolaceOperationTopicModel}
import amf.apicontract.internal.metamodel.domain.bindings.SolaceOperationBindingModel._
import amf.apicontract.internal.spec.async.parser.bindings.Bindings.Solace
import amf.core.client.scala.model.domain.{DomainElement, Linkable, NamedDomainElement}
import amf.core.client.scala.model.{BoolField, IntField, StrField}
import amf.core.internal.metamodel.{Field, Obj}
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

abstract class SolaceOperationDestination(override val fields: Fields, override val annotations: Annotations)
    extends DomainElement {

  def destinationType: StrField   = fields.field(SolaceOperationDestinationModel.DestinationType)
  def deliveryMode: StrField      = fields.field(SolaceOperationDestinationModel.DeliveryMode)
  def queue: SolaceOperationQueue = fields.field(SolaceOperationDestinationModel.Queue)

  def withDestinationType(destinationType: String): this.type =
    set(SolaceOperationDestinationModel.DestinationType, destinationType)
  def withDeliveryMode(deliveryMode: String): this.type =
    set(SolaceOperationDestinationModel.DeliveryMode, deliveryMode)
  def withQueue(queue: SolaceOperationQueue): this.type = set(SolaceOperationDestinationModel.Queue, queue)

  override def componentId: String = s"/$Solace-destination"
}
class SolaceOperationDestination010(override val fields: Fields, override val annotations: Annotations)
    extends SolaceOperationDestination(fields, annotations) {
  override def componentId: String             = "/solace-operation-destination-010"
  override def meta: SolaceOperationDestination010Model.type = SolaceOperationDestination010Model

}
object SolaceOperationDestination010 {
  def apply(): SolaceOperationDestination010                         = apply(Annotations())
  def apply(annotations: Annotations): SolaceOperationDestination010 = apply(Fields(), annotations)
  def apply(fields: Fields, annotations: Annotations): SolaceOperationDestination010 =
    new SolaceOperationDestination010(fields, annotations)
}
class SolaceOperationDestination020(override val fields: Fields, override val annotations: Annotations)
    extends SolaceOperationDestination(fields, annotations) {
  override def meta: SolaceOperationDestination020Model.type = SolaceOperationDestination020Model
  override def componentId: String                           = s"/$Solace-operation-destination-020"
  def topic: SolaceOperationTopic                            = fields.field(SolaceOperationDestination020Model.Topic)
  def withTopic(topic: SolaceOperationTopic): this.type      = set(SolaceOperationDestination020Model.Topic, topic)
}

object SolaceOperationDestination020 {
  def apply(): SolaceOperationDestination020                         = apply(Annotations())
  def apply(annotations: Annotations): SolaceOperationDestination020 = apply(Fields(), annotations)
  def apply(fields: Fields, annotations: Annotations): SolaceOperationDestination020 =
    new SolaceOperationDestination020(fields, annotations)
}

class SolaceOperationQueue(override val fields: Fields, override val annotations: Annotations)
    extends DomainElement
    with NamedDomainElement {
  override def meta: SolaceOperationQueueModel.type = SolaceOperationQueueModel

  override def nameField: Field = SolaceOperationQueueModel.Name

  def topicSubscriptions: Seq[StrField] = fields.field(SolaceOperationQueueModel.TopicSubscriptions)
  def accessType: StrField              = fields.field(SolaceOperationQueueModel.AccessType)

  def withTopicSubscriptions(topicSubscriptions: Seq[String]): this.type =
    set(SolaceOperationQueueModel.TopicSubscriptions, topicSubscriptions)
  def withAccessType(accessType: String): this.type = set(SolaceOperationQueueModel.AccessType, accessType)

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
