package amf.apicontract.client.scala.model.domain.bindings.solace

import amf.apicontract.client.scala.model.domain.bindings.{BindingVersion, OperationBinding}
import amf.apicontract.internal.metamodel.domain.bindings.{SolaceOperationBinding010Model, SolaceOperationBinding020Model, SolaceOperationBinding030Model, SolaceOperationBinding040Model, SolaceOperationBindingModel, SolaceOperationDestination010Model, SolaceOperationDestination020Model, SolaceOperationDestination030Model, SolaceOperationDestination040Model, SolaceOperationDestinationModel, SolaceOperationQueue010Model, SolaceOperationQueue030Model, SolaceOperationQueueModel, SolaceOperationTopicModel}
import amf.apicontract.internal.metamodel.domain.bindings.SolaceOperationBindingModel._
import amf.apicontract.internal.spec.async.parser.bindings.Bindings.Solace
import amf.core.client.scala.model.domain.{DomainElement, Linkable, NamedDomainElement}
import amf.core.client.scala.model.{BoolField, IntField, StrField}
import amf.core.internal.metamodel.{Field, Obj}
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.shapes.client.scala.model.domain.Key
// Operation Binding
abstract class SolaceOperationBinding(override val fields: Fields, override val annotations: Annotations)
    extends OperationBinding
    with BindingVersion
    with Key {
  override protected def bindingVersionField: Field = BindingVersion
  override def key: StrField                        = fields.field(SolaceOperationBindingModel.key)
  override def componentId: String                  = s"/$Solace-operation"
  def destinations: Seq[SolaceOperationDestination] = fields.field(Destinations)
  def withDestinations(destinations: Seq[SolaceOperationDestination]): this.type = setArray(Destinations, destinations)
}
class SolaceOperationBinding010(fields: Fields, annotations: Annotations)
    extends SolaceOperationBinding(fields, annotations) {
  override def destinations: Seq[SolaceOperationDestination010] =
    fields.field(SolaceOperationBinding010Model.Destinations)
  def withDestinations(destinations: Seq[SolaceOperationDestination010]): this.type =
    setArray(Destinations, destinations)
  override def componentId: String                = s"/$Solace-operation-010"
  def meta: SolaceOperationBinding010Model.type   = SolaceOperationBinding010Model
  override def linkCopy(): SolaceOperationBinding = SolaceOperationBinding010().withId(id)

  /** apply method for create a new instance with fields and annotations. Aux method for copy */
  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement =
    SolaceOperationBinding010.apply
}
object SolaceOperationBinding010 {
  def apply(): SolaceOperationBinding010                         = apply(Annotations())
  def apply(annotations: Annotations): SolaceOperationBinding010 = apply(Fields(), annotations)
  def apply(fields: Fields, annotations: Annotations): SolaceOperationBinding010 =
    new SolaceOperationBinding010(fields, annotations)
}
class SolaceOperationBinding020(fields: Fields, annotations: Annotations)
    extends SolaceOperationBinding(fields, annotations) {
  override def destinations: Seq[SolaceOperationDestination020] =
    fields.field(SolaceOperationBinding020Model.Destinations)
  def withDestinations(destinations: Seq[SolaceOperationDestination020]): this.type =
    setArray(Destinations, destinations)
  override def componentId: String                = s"/$Solace-operation-020"
  def meta: SolaceOperationBinding020Model.type   = SolaceOperationBinding020Model
  override def linkCopy(): SolaceOperationBinding = SolaceOperationBinding020().withId(id)

  /** apply method for create a new instance with fields and annotations. Aux method for copy */
  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement =
    SolaceOperationBinding020.apply
}
object SolaceOperationBinding020 {
  def apply(): SolaceOperationBinding020                         = apply(Annotations())
  def apply(annotations: Annotations): SolaceOperationBinding020 = apply(Fields(), annotations)
  def apply(fields: Fields, annotations: Annotations): SolaceOperationBinding020 =
    new SolaceOperationBinding020(fields, annotations)
}
class SolaceOperationBinding030(fields: Fields, annotations: Annotations)
    extends SolaceOperationBinding(fields, annotations) {
  override def destinations: Seq[SolaceOperationDestination030] =
    fields.field(SolaceOperationBinding030Model.Destinations)
  def withDestinations(destinations: Seq[SolaceOperationDestination030]): this.type =
    setArray(Destinations, destinations)
  override def componentId: String                = s"/$Solace-operation-030"
  def meta: SolaceOperationBinding030Model.type   = SolaceOperationBinding030Model
  override def linkCopy(): SolaceOperationBinding = SolaceOperationBinding030().withId(id)

  /** apply method for create a new instance with fields and annotations. Aux method for copy */
  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement =
    SolaceOperationBinding030.apply
}
object SolaceOperationBinding030 {
  def apply(): SolaceOperationBinding030                         = apply(Annotations())
  def apply(annotations: Annotations): SolaceOperationBinding030 = apply(Fields(), annotations)
  def apply(fields: Fields, annotations: Annotations): SolaceOperationBinding030 =
    new SolaceOperationBinding030(fields, annotations)
}
class SolaceOperationBinding040(fields: Fields, annotations: Annotations)
  extends SolaceOperationBinding(fields, annotations) {

  override def destinations: Seq[SolaceOperationDestination040] = fields.field(SolaceOperationBinding040Model.Destinations)

  def withDestinations(destinations: Seq[SolaceOperationDestination040]): this.type = setArray(Destinations, destinations)

  def timeToLive: IntField = fields.field(SolaceOperationBinding040Model.TimeToLive)
  def priority: IntField = fields.field(SolaceOperationBinding040Model.Priority)
  def dmqEligible: BoolField = fields.field(SolaceOperationBinding040Model.DmqEligible)

  def withTimeToLive(value: Int): this.type = set(SolaceOperationBinding040Model.TimeToLive, value)
  def withPriority(value: Int): this.type = set(SolaceOperationBinding040Model.Priority, value)
  def withDmqEligible(value: Boolean): this.type = set(SolaceOperationBinding040Model.DmqEligible, value)

  override def componentId: String = s"/$Solace-operation-040"
  override def meta: SolaceOperationBinding040Model.type = SolaceOperationBinding040Model
  override def linkCopy(): SolaceOperationBinding = SolaceOperationBinding040().withId(id)

  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement = SolaceOperationBinding040.apply
}
object SolaceOperationBinding040 {
  def apply(): SolaceOperationBinding040 = apply(Annotations())
  def apply(annotations: Annotations): SolaceOperationBinding040 = apply(Fields(), annotations)
  def apply(fields: Fields, annotations: Annotations): SolaceOperationBinding040 =
    new SolaceOperationBinding040(fields, annotations)
}

//Operation Destination
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
  override def queue: SolaceOperationQueue010 = fields.field(SolaceOperationDestination010Model.Queue)
  def withQueue(queue: SolaceOperationQueue010): this.type = set(SolaceOperationDestination010Model.Queue, queue)
  override def componentId: String                           = s"/$Solace-operation-destination-010"
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
  override def queue: SolaceOperationQueue010 = fields.field(SolaceOperationDestination020Model.Queue) //se le pasa la queue de 010, la del 020 es la misma
  def withQueue(queue: SolaceOperationQueue010): this.type = set(SolaceOperationDestination020Model.Queue, queue)
  def topic: SolaceOperationTopic                            = fields.field(SolaceOperationDestination020Model.Topic)
  def withTopic(topic: SolaceOperationTopic): this.type      = set(SolaceOperationDestination020Model.Topic, topic)
  def destinations: Seq[SolaceOperationDestination020]       = fields.field(SolaceOperationBinding020Model.Destinations)
  def withDestinations(destinations: Seq[SolaceOperationDestination020]): this.type = setArray(SolaceOperationBinding020Model.Destinations, destinations)
}

object SolaceOperationDestination020 {
  def apply(): SolaceOperationDestination020                         = apply(Annotations())
  def apply(annotations: Annotations): SolaceOperationDestination020 = apply(Fields(), annotations)
  def apply(fields: Fields, annotations: Annotations): SolaceOperationDestination020 =
    new SolaceOperationDestination020(fields, annotations)
}

class SolaceOperationDestination030(override val fields: Fields, override val annotations: Annotations)
    extends SolaceOperationDestination(fields, annotations) {

  override def withDestinationType(destinationType: String): SolaceOperationDestination030.this.type = super.withDestinationType(destinationType)
  override def meta: SolaceOperationDestination030Model.type = SolaceOperationDestination030Model
  override def componentId: String                           = s"/$Solace-operation-destination-030"
  override def queue: SolaceOperationQueue030 = fields.field(SolaceOperationDestination030Model.Queue)
  def withQueue(queue: SolaceOperationQueue030): this.type = set(SolaceOperationDestination030Model.Queue, queue)
  def destinations: Seq[SolaceOperationDestination030]       = fields.field(SolaceOperationBinding030Model.Destinations) //proque tiene destinations?
  def withDestinations(destinations: Seq[SolaceOperationDestination030]): this.type = setArray(SolaceOperationBinding030Model.Destinations, destinations)
  def topic: SolaceOperationTopic                            = fields.field(SolaceOperationDestination030Model.Topic)
  def withTopic(topic: SolaceOperationTopic): this.type      = set(SolaceOperationDestination030Model.Topic, topic)
}

object SolaceOperationDestination030 {
  def apply(): SolaceOperationDestination030                         = apply(Annotations())
  def apply(annotations: Annotations): SolaceOperationDestination030 = apply(Fields(), annotations)
  def apply(fields: Fields, annotations: Annotations): SolaceOperationDestination030 =
    new SolaceOperationDestination030(fields, annotations)
}

class SolaceOperationDestination040(override val fields: Fields, override val annotations: Annotations)
  extends SolaceOperationDestination(fields, annotations) {
  override def meta: SolaceOperationDestination040Model.type = SolaceOperationDestination040Model
  override def componentId: String = s"/$Solace-operation-destination-040"
  override def queue: SolaceOperationQueue030 = fields.field(SolaceOperationDestination040Model.Queue)
  def withQueue(queue: SolaceOperationQueue030): this.type = set(SolaceOperationDestination040Model.Queue, queue)
  def topic: SolaceOperationTopic                            = fields.field(SolaceOperationDestination040Model.Topic)
  def withTopic(topic: SolaceOperationTopic): this.type      = set(SolaceOperationDestination040Model.Topic, topic)
  def destinations: Seq[SolaceOperationDestination040]       = fields.field(SolaceOperationBinding040Model.Destinations)
  def withDestinations(destinations: Seq[SolaceOperationDestination040]): this.type = setArray(SolaceOperationBinding040Model.Destinations, destinations)
  def withBindingVersion(bindingVersion: String): this.type = set(SolaceOperationDestination040Model.BindingVersion, bindingVersion)
  def bindingVersion: StrField = fields.field(SolaceOperationDestination040Model.BindingVersion)
}

object SolaceOperationDestination040 {
  def apply(): SolaceOperationDestination040 = apply(Annotations())
  def apply(annotations: Annotations): SolaceOperationDestination040 = apply(Fields(), annotations)
  def apply(fields: Fields, annotations: Annotations): SolaceOperationDestination040 =
    new SolaceOperationDestination040(fields, annotations)
}
// Operation Queue
abstract class SolaceOperationQueue(override val fields: Fields, override val annotations: Annotations)
    extends DomainElement
    with NamedDomainElement {

  override def nameField: Field = SolaceOperationQueueModel.Name

  def topicSubscriptions: Seq[StrField] = fields.field(SolaceOperationQueueModel.TopicSubscriptions)
  def accessType: StrField              = fields.field(SolaceOperationQueueModel.AccessType)

  def withTopicSubscriptions(topicSubscriptions: Seq[String]): this.type =
    set(SolaceOperationQueueModel.TopicSubscriptions, topicSubscriptions)
  def withAccessType(accessType: String): this.type = set(SolaceOperationQueueModel.AccessType, accessType)

}

class SolaceOperationQueue010(override val fields: Fields, override val annotations: Annotations)
    extends SolaceOperationQueue(fields, annotations) {
  override def componentId: String                     = s"/$Solace-operation-queue-010"
  override def meta: SolaceOperationQueue010Model.type = SolaceOperationQueue010Model
}

object SolaceOperationQueue010 {
  def apply(): SolaceOperationQueue010 = apply(Annotations())

  def apply(annotations: Annotations): SolaceOperationQueue010 = apply(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): SolaceOperationQueue010 =
    new SolaceOperationQueue010(fields, annotations)
}
class SolaceOperationQueue030(override val fields: Fields, override val annotations: Annotations)
    extends SolaceOperationQueue(fields, annotations) {
  override def componentId: String                     = s"/$Solace-operation-queue-030"
  override def meta: SolaceOperationQueue030Model.type = SolaceOperationQueue030Model
  def maxMsgSpoolSize: StrField                        = fields.field(SolaceOperationQueue030Model.MaxMsgSpoolSize)
  def maxTtl: StrField                                 = fields.field(SolaceOperationQueue030Model.MaxTtl)

  def withMaxMsgSpoolSize(value: String): this.type = set(SolaceOperationQueue030Model.MaxMsgSpoolSize, value)
  def withMaxTtl(value: String): this.type          = set(SolaceOperationQueue030Model.MaxTtl, value)
}

object SolaceOperationQueue030 {
  def apply(): SolaceOperationQueue030 = apply(Annotations())

  def apply(annotations: Annotations): SolaceOperationQueue030 = apply(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): SolaceOperationQueue030 =
    new SolaceOperationQueue030(fields, annotations)
}
//Operation Topic
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
