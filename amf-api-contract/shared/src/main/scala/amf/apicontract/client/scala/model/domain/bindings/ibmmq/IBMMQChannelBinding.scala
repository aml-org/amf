package amf.apicontract.client.scala.model.domain.bindings.ibmmq

import amf.apicontract.client.scala.model.domain.bindings.{BindingVersion, ChannelBinding}
import amf.apicontract.internal.metamodel.domain.bindings.{
  IBMMQChannelBindingModel,
  IBMMQChannelQueueModel,
  IBMMQChannelTopicModel
}
import amf.apicontract.internal.metamodel.domain.bindings.IBMMQChannelBindingModel._
import amf.apicontract.internal.spec.async.parser.bindings.Bindings.IBMMQ
import amf.core.client.scala.model.domain.{DomainElement, Linkable, NamedDomainElement}
import amf.core.client.scala.model.{BoolField, IntField, StrField}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.shapes.client.scala.model.domain.Key

class IBMMQChannelBinding(override val fields: Fields, override val annotations: Annotations)
    extends ChannelBinding
    with BindingVersion
    with Key {

  override protected def bindingVersionField: Field = BindingVersion
  override def meta: IBMMQChannelBindingModel.type  = IBMMQChannelBindingModel

  def destinationType: StrField = fields.field(DestinationType)
  def queue: IBMMQChannelQueue  = fields.field(Queue)
  def topic: IBMMQChannelTopic  = fields.field(Topic)
  def maxMsgLength: IntField    = fields.field(MaxMsgLength)

  def withDestinationType(destinationType: String): this.type = set(DestinationType, destinationType)
  def withQueue(queue: IBMMQChannelQueue): this.type          = set(Queue, queue)
  def withTopic(topic: IBMMQChannelTopic): this.type          = set(Topic, topic)
  def withMaxMsgLength(maxMsgLength: Int): this.type          = set(MaxMsgLength, maxMsgLength)

  override def key: StrField = fields.field(IBMMQChannelBindingModel.key)

  override def componentId: String = s"/$IBMMQ-channel"

  override def linkCopy(): IBMMQChannelBinding = IBMMQChannelBinding()

  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement =
    IBMMQChannelBinding.apply
}

object IBMMQChannelBinding {

  def apply(): IBMMQChannelBinding = apply(Annotations())

  def apply(annotations: Annotations): IBMMQChannelBinding = apply(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): IBMMQChannelBinding =
    new IBMMQChannelBinding(fields, annotations)
}

class IBMMQChannelQueue(override val fields: Fields, override val annotations: Annotations)
    extends DomainElement
    with NamedDomainElement {
  override def meta: IBMMQChannelQueueModel.type = IBMMQChannelQueueModel

  override def nameField: Field = IBMMQChannelQueueModel.Name

  def objectName: StrField     = fields.field(IBMMQChannelQueueModel.ObjectName)
  def isPartitioned: BoolField = fields.field(IBMMQChannelQueueModel.IsPartitioned)
  def exclusive: BoolField     = fields.field(IBMMQChannelQueueModel.Exclusive)

  def withObjectName(objectName: Boolean): this.type       = set(IBMMQChannelQueueModel.ObjectName, objectName)
  def withIsPartitioned(isPartitioned: Boolean): this.type = set(IBMMQChannelQueueModel.IsPartitioned, isPartitioned)
  def withExclusive(exclusive: Boolean): this.type         = set(IBMMQChannelQueueModel.Exclusive, exclusive)

  override def componentId: String = s"/$IBMMQ-queue"
}

object IBMMQChannelQueue {

  def apply(): IBMMQChannelQueue = apply(Annotations())

  def apply(annotations: Annotations): IBMMQChannelQueue = apply(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): IBMMQChannelQueue = new IBMMQChannelQueue(fields, annotations)
}

class IBMMQChannelTopic(override val fields: Fields, override val annotations: Annotations)
    extends DomainElement
    with NamedDomainElement {
  override def meta: IBMMQChannelTopicModel.type = IBMMQChannelTopicModel

  override def nameField: Field = IBMMQChannelTopicModel.Name

  def string: BoolField           = fields.field(IBMMQChannelTopicModel.String)
  def objectName: StrField        = fields.field(IBMMQChannelTopicModel.ObjectName)
  def durablePermitted: BoolField = fields.field(IBMMQChannelTopicModel.DurablePermitted)
  def lastMsgRetained: BoolField  = fields.field(IBMMQChannelTopicModel.LastMsgRetained)

  def withString(string: Boolean): this.type         = set(IBMMQChannelTopicModel.String, string)
  def withObjectName(objectName: Boolean): this.type = set(IBMMQChannelTopicModel.ObjectName, objectName)
  def withDurablePermitted(durablePermitted: Boolean): this.type =
    set(IBMMQChannelTopicModel.DurablePermitted, durablePermitted)
  def withLastMsgRetained(lastMsgRetained: Boolean): this.type =
    set(IBMMQChannelTopicModel.LastMsgRetained, lastMsgRetained)

  override def componentId: String = s"/$IBMMQ-topic"
}

object IBMMQChannelTopic {

  def apply(): IBMMQChannelTopic = apply(Annotations())

  def apply(annotations: Annotations): IBMMQChannelTopic = apply(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): IBMMQChannelTopic = new IBMMQChannelTopic(fields, annotations)
}
