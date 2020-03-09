package amf.plugins.domain.webapi.models.bindings.amqp

import amf.core.metamodel.{Field, Obj}
import amf.core.model.domain.{DomainElement, Linkable, NamedDomainElement}
import amf.core.model.{BoolField, StrField}
import amf.core.parser.{Annotations, Fields}
import amf.plugins.domain.webapi.metamodel.bindings.Amqp091ChannelBindingModel._
import amf.plugins.domain.webapi.metamodel.bindings.{
  Amqp091ChannelBindingModel,
  Amqp091ChannelExchangeModel => ChannelExchange,
  Amqp091QueueModel => QueueModel
}
import amf.plugins.domain.webapi.models.Key
import amf.plugins.domain.webapi.models.bindings.{BindingVersion, ChannelBinding}

class Amqp091ChannelBinding(override val fields: Fields, override val annotations: Annotations)
    extends ChannelBinding
    with BindingVersion
    with Key {

  override def meta: Obj = Amqp091ChannelBindingModel

  override def componentId: String = "/amqp091-channel"

  override def key: StrField = fields.field(Amqp091ChannelBindingModel.key)

  override protected def bindingVersionField: Field = BindingVersion

  def is: StrField                                              = fields.field(Is)
  def exchange: Amqp091ChannelExchange                          = fields.field(Exchange)
  def queue: Amqp091Queue                                       = fields.field(Queue)
  def withIs(is: String): this.type                             = set(Is, is)
  def withExchange(exchange: Amqp091ChannelExchange): this.type = set(Exchange, exchange)
  def withQueue(queue: Amqp091Queue): this.type                 = set(Queue, queue)

  override def linkCopy(): Amqp091ChannelBinding = Amqp091ChannelBinding().withId(id)

  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement =
    Amqp091ChannelBinding.apply
}

object Amqp091ChannelBinding {

  def apply(): Amqp091ChannelBinding = apply(Annotations())

  def apply(annotations: Annotations): Amqp091ChannelBinding = apply(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): Amqp091ChannelBinding =
    new Amqp091ChannelBinding(fields, annotations)
}

class Amqp091ChannelExchange(override val fields: Fields, override val annotations: Annotations)
    extends DomainElement
    with NamedDomainElement {
  override def meta: Obj = ChannelExchange

  override protected def nameField: Field = ChannelExchange.Name
  def `type`: StrField                    = fields.field(ChannelExchange.Type)
  def durable: BoolField                  = fields.field(ChannelExchange.Durable)
  def autoDelete: BoolField               = fields.field(ChannelExchange.AutoDelete)
  def vHost: StrField                     = fields.field(ChannelExchange.VHost)

  def withType(`type`: String): this.type            = set(ChannelExchange.Type, `type`)
  def withDurable(durable: Boolean): this.type       = set(ChannelExchange.Durable, durable)
  def withAutoDelete(autoDelete: Boolean): this.type = set(ChannelExchange.AutoDelete, autoDelete)
  def withVHost(vHost: String): this.type            = set(ChannelExchange.VHost, vHost)

  override def componentId: String = "/amqp091-exchange"
}

object Amqp091ChannelExchange {

  def apply(): Amqp091ChannelExchange = apply(Annotations())

  def apply(annotations: Annotations): Amqp091ChannelExchange = apply(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): Amqp091ChannelExchange =
    new Amqp091ChannelExchange(fields, annotations)
}

class Amqp091Queue(override val fields: Fields, override val annotations: Annotations)
    extends DomainElement
    with NamedDomainElement {
  override def meta: Obj = QueueModel

  override protected def nameField: Field = QueueModel.Name

  def durable: BoolField    = fields.field(QueueModel.Durable)
  def exclusive: BoolField  = fields.field(QueueModel.Exclusive)
  def autoDelete: BoolField = fields.field(QueueModel.AutoDelete)
  def vHost: StrField       = fields.field(QueueModel.VHost)

  def withDurable(durable: Boolean): this.type       = set(QueueModel.Durable, durable)
  def withExclusive(exclusive: Boolean): this.type   = set(QueueModel.Exclusive, exclusive)
  def withAutoDelete(autoDelete: Boolean): this.type = set(QueueModel.AutoDelete, autoDelete)
  def withVHost(vHost: String): this.type            = set(QueueModel.VHost, vHost)

  override def componentId: String = "/amqp091-queue"
}

object Amqp091Queue {

  def apply(): Amqp091Queue = apply(Annotations())

  def apply(annotations: Annotations): Amqp091Queue = apply(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): Amqp091Queue = new Amqp091Queue(fields, annotations)
}
