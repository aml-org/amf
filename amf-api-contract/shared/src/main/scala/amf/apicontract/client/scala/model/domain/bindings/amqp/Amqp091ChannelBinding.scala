package amf.apicontract.client.scala.model.domain.bindings.amqp

import amf.core.client.scala.model.domain.{DomainElement, Linkable, NamedDomainElement}
import amf.core.client.scala.model.{BoolField, StrField}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.apicontract.internal.metamodel.domain.bindings.Amqp091ChannelBinding020Model._
import amf.apicontract.internal.metamodel.domain.bindings.{
  Amqp091ChannelBindingModel => BaseModel,
  Amqp091ChannelBinding010Model => Amqp010Model,
  Amqp091ChannelBinding020Model => Amqp020Model,
  Amqp091ChannelExchangeModel => BaseExchangeModel,
  Amqp091ChannelExchange010Model => Exchange010Model,
  Amqp091ChannelExchange020Model => Exchange020Model,
  Amqp091QueueModel => BaseQueueModel,
  Amqp091Queue010Model => Queue010Model,
  Amqp091Queue020Model => Queue020Model
}
import amf.apicontract.client.scala.model.domain.bindings.{BindingVersion, ChannelBinding}
import amf.shapes.client.scala.model.domain.Key

abstract class Amqp091ChannelBinding(override val fields: Fields, override val annotations: Annotations)
    extends ChannelBinding
    with BindingVersion
    with Key {
  override def key: StrField                        = fields.field(BaseModel.key)
  override protected def bindingVersionField: Field = BindingVersion

  def is: StrField                     = fields.field(BaseModel.Is)
  def exchange: Amqp091ChannelExchange = fields.field(BaseModel.Exchange)
  def queue: Amqp091Queue              = fields.field(BaseModel.Queue)

  def withIs(is: String): this.type                             = set(BaseModel.Is, is)
  def withExchange(exchange: Amqp091ChannelExchange): this.type = set(BaseModel.Exchange, exchange)
  def withQueue(queue: Amqp091Queue): this.type                 = set(BaseModel.Queue, queue)
}

abstract class Amqp091ChannelExchange(override val fields: Fields, override val annotations: Annotations)
    extends DomainElement
    with NamedDomainElement {
  override def nameField: Field = BaseExchangeModel.Name

  def `type`: StrField      = fields.field(BaseExchangeModel.Type)
  def durable: BoolField    = fields.field(BaseExchangeModel.Durable)
  def autoDelete: BoolField = fields.field(BaseExchangeModel.AutoDelete)

  def withType(`type`: String): this.type            = set(BaseExchangeModel.Type, `type`)
  def withDurable(durable: Boolean): this.type       = set(BaseExchangeModel.Durable, durable)
  def withAutoDelete(autoDelete: Boolean): this.type = set(BaseExchangeModel.AutoDelete, autoDelete)
}

abstract class Amqp091Queue(override val fields: Fields, override val annotations: Annotations)
    extends DomainElement
    with NamedDomainElement {
  override def nameField: Field = BaseQueueModel.Name

  def durable: BoolField    = fields.field(BaseQueueModel.Durable)
  def exclusive: BoolField  = fields.field(BaseQueueModel.Exclusive)
  def autoDelete: BoolField = fields.field(BaseQueueModel.AutoDelete)

  def withDurable(durable: Boolean): this.type       = set(BaseQueueModel.Durable, durable)
  def withExclusive(exclusive: Boolean): this.type   = set(BaseQueueModel.Exclusive, exclusive)
  def withAutoDelete(autoDelete: Boolean): this.type = set(BaseQueueModel.AutoDelete, autoDelete)
}

class Amqp091ChannelBinding010(override val fields: Fields, override val annotations: Annotations)
    extends Amqp091ChannelBinding(fields, annotations) {

  override def meta: Amqp010Model.type = Amqp010Model

  override def componentId: String = "/amqp091-channel-010"

  override def key: StrField = fields.field(Amqp010Model.key)

  override def exchange: Amqp091ChannelExchange010                 = fields.field(Amqp010Model.Exchange)
  override def queue: Amqp091Queue010                              = fields.field(Amqp010Model.Queue)
  def withExchange(exchange: Amqp091ChannelExchange010): this.type = set(Amqp010Model.Exchange, exchange)
  def withQueue(queue: Amqp091Queue010): this.type                 = set(Amqp010Model.Queue, queue)

  override def linkCopy(): Amqp091ChannelBinding010 = Amqp091ChannelBinding010().withId(id)

  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement =
    Amqp091ChannelBinding010.apply
}

object Amqp091ChannelBinding010 {
  def apply(): Amqp091ChannelBinding010 = apply(Annotations())

  def apply(annotations: Annotations): Amqp091ChannelBinding010 = apply(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): Amqp091ChannelBinding010 =
    new Amqp091ChannelBinding010(fields, annotations)
}

class Amqp091ChannelExchange010(override val fields: Fields, override val annotations: Annotations)
    extends Amqp091ChannelExchange(fields, annotations) {
  override def meta: Exchange010Model.type = Exchange010Model

  override def componentId: String = "/amqp091-exchange-010"
}

object Amqp091ChannelExchange010 {
  def apply(): Amqp091ChannelExchange010 = apply(Annotations())

  def apply(annotations: Annotations): Amqp091ChannelExchange010 = apply(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): Amqp091ChannelExchange010 =
    new Amqp091ChannelExchange010(fields, annotations)
}

class Amqp091Queue010(override val fields: Fields, override val annotations: Annotations)
    extends Amqp091Queue(fields, annotations) {
  override def meta: Queue010Model.type = Queue010Model

  override def componentId: String = "/amqp091-queue-010"
}

object Amqp091Queue010 {
  def apply(): Amqp091Queue010 = apply(Annotations())

  def apply(annotations: Annotations): Amqp091Queue010 = apply(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): Amqp091Queue010 = new Amqp091Queue010(fields, annotations)
}

class Amqp091ChannelBinding020(override val fields: Fields, override val annotations: Annotations)
    extends Amqp091ChannelBinding(fields, annotations) {

  override def meta: Amqp020Model.type = Amqp020Model

  override def componentId: String = "/amqp091-channel-020"

  override def key: StrField = fields.field(Amqp020Model.key)

  override def exchange: Amqp091ChannelExchange020                 = fields.field(Amqp020Model.Exchange)
  override def queue: Amqp091Queue020                              = fields.field(Amqp020Model.Queue)
  def withExchange(exchange: Amqp091ChannelExchange020): this.type = set(Amqp020Model.Exchange, exchange)
  def withQueue(queue: Amqp091Queue020): this.type                 = set(Amqp020Model.Queue, queue)

  override def linkCopy(): Amqp091ChannelBinding020 = Amqp091ChannelBinding020().withId(id)

  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement =
    Amqp091ChannelBinding020.apply
}

object Amqp091ChannelBinding020 {
  def apply(): Amqp091ChannelBinding020 = apply(Annotations())

  def apply(annotations: Annotations): Amqp091ChannelBinding020 = apply(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): Amqp091ChannelBinding020 =
    new Amqp091ChannelBinding020(fields, annotations)
}

class Amqp091Queue020(override val fields: Fields, override val annotations: Annotations)
    extends Amqp091Queue(fields, annotations) {
  override def meta: Queue020Model.type = Queue020Model

  def vHost: StrField                     = fields.field(Queue020Model.VHost)
  def withVHost(vHost: String): this.type = set(Queue020Model.VHost, vHost)

  override def componentId: String = "/amqp091-queue-020"
}

object Amqp091Queue020 {
  def apply(): Amqp091Queue020 = apply(Annotations())

  def apply(annotations: Annotations): Amqp091Queue020 = apply(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): Amqp091Queue020 = new Amqp091Queue020(fields, annotations)
}

class Amqp091ChannelExchange020(override val fields: Fields, override val annotations: Annotations)
    extends Amqp091ChannelExchange(fields, annotations) {
  override def meta: Exchange020Model.type = Exchange020Model

  def vHost: StrField                     = fields.field(Exchange020Model.VHost)
  def withVHost(vHost: String): this.type = set(Exchange020Model.VHost, vHost)

  override def componentId: String = "/amqp091-exchange-020"
}

object Amqp091ChannelExchange020 {
  def apply(): Amqp091ChannelExchange020 = apply(Annotations())

  def apply(annotations: Annotations): Amqp091ChannelExchange020 = apply(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): Amqp091ChannelExchange020 =
    new Amqp091ChannelExchange020(fields, annotations)
}
