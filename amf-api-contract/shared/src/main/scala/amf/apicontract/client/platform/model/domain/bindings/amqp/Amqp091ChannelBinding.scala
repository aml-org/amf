package amf.apicontract.client.platform.model.domain.bindings.amqp

import amf.apicontract.client.platform.model.domain.bindings.{BindingVersion, ChannelBinding}
import amf.apicontract.client.scala.model.domain.bindings.amqp.{
  Amqp091ChannelBinding => InternalAmqp091ChannelBinding,
  Amqp091ChannelExchange => InternalAmqp091ChannelExchange,
  Amqp091Queue => InternalAmqp091Queue,
  Amqp091ChannelBinding010 => InternalAmqp091ChannelBinding010,
  Amqp091ChannelExchange010 => InternalAmqp091ChannelExchange010,
  Amqp091Queue010 => InternalAmqp091Queue010,
  Amqp091ChannelBinding020 => InternalAmqp091ChannelBinding020,
  Amqp091ChannelExchange020 => InternalAmqp091ChannelExchange020,
  Amqp091Queue020 => InternalAmqp091Queue020
}
import amf.apicontract.internal.convert.ApiClientConverters._
import amf.core.client.platform.model.domain.{DomainElement, NamedDomainElement}
import amf.core.client.platform.model.{BoolField, StrField}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
abstract class Amqp091ChannelBinding(override private[amf] val _internal: InternalAmqp091ChannelBinding)
    extends ChannelBinding
    with BindingVersion {

  override protected def bindingVersion: StrField = _internal.bindingVersion
  override def withBindingVersion(bindingVersion: String): this.type = {
    _internal.withBindingVersion(bindingVersion)
    this
  }

  def is: StrField                     = _internal.is
  def exchange: Amqp091ChannelExchange = _internal.exchange
  def queue: Amqp091Queue              = _internal.queue
  def withIs(is: String): this.type = {
    _internal.withIs(is)
    this
  }
  def withExchange(exchange: Amqp091ChannelExchange): this.type = {
    _internal.withExchange(exchange)
    this
  }
  def withQueue(queue: Amqp091Queue): this.type = {
    _internal.withQueue(queue)
    this
  }
}

@JSExportAll
abstract class Amqp091ChannelExchange(override private[amf] val _internal: InternalAmqp091ChannelExchange)
    extends DomainElement
    with NamedDomainElement {

  def `type`: StrField      = _internal.`type`
  def durable: BoolField    = _internal.durable
  def autoDelete: BoolField = _internal.autoDelete

  def withType(`type`: String): this.type = {
    _internal.withType(`type`)
    this
  }
  def withDurable(durable: Boolean): this.type = {
    _internal.withDurable(durable)
    this
  }
  def withAutoDelete(autoDelete: Boolean): this.type = {
    _internal.withAutoDelete(autoDelete)
    this
  }

  /** Return DomainElement name. */
  override def name: StrField = _internal.name

  /** Update DomainElement name. */
  override def withName(name: String): this.type = {
    _internal.withName(name)
    this
  }
}

@JSExportAll
abstract class Amqp091Queue(override private[amf] val _internal: InternalAmqp091Queue)
    extends DomainElement
    with NamedDomainElement {

  def durable: BoolField    = _internal.durable
  def exclusive: BoolField  = _internal.exclusive
  def autoDelete: BoolField = _internal.autoDelete

  def withDurable(durable: Boolean): this.type = {
    _internal.withDurable(durable)
    this
  }
  def withExclusive(exclusive: Boolean): this.type = {
    _internal.withExclusive(exclusive)
    this
  }
  def withAutoDelete(autoDelete: Boolean): this.type = {
    _internal.withAutoDelete(autoDelete)
    this
  }

  override def name: StrField = _internal.name

  override def withName(name: String): this.type = {
    _internal.withName(name)
    this
  }
}

@JSExportAll
case class Amqp091ChannelBinding010(override private[amf] val _internal: InternalAmqp091ChannelBinding010)
    extends Amqp091ChannelBinding(_internal) {

  @JSExportTopLevel("Amqp091ChannelBinding010")
  def this() = this(InternalAmqp091ChannelBinding010())

  override protected def bindingVersion: StrField = _internal.bindingVersion
  override def withBindingVersion(bindingVersion: String): this.type = {
    _internal.withBindingVersion(bindingVersion)
    this
  }

  override def exchange: Amqp091ChannelExchange010 = _internal.exchange
  override def queue: Amqp091Queue010              = _internal.queue

  def withExchange(exchange: Amqp091ChannelExchange010): this.type = {
    _internal.withExchange(exchange)
    this
  }
  def withQueue(queue: Amqp091Queue010): this.type = {
    _internal.withQueue(queue)
    this
  }

  override def linkCopy(): Amqp091ChannelBinding010 = _internal.linkCopy()
}

@JSExportAll
case class Amqp091ChannelExchange010(override private[amf] val _internal: InternalAmqp091ChannelExchange010)
    extends Amqp091ChannelExchange(_internal) {

  @JSExportTopLevel("Amqp091ChannelExchange010")
  def this() = this(InternalAmqp091ChannelExchange010())
}

@JSExportAll
case class Amqp091Queue010(override private[amf] val _internal: InternalAmqp091Queue010)
    extends Amqp091Queue(_internal) {

  @JSExportTopLevel("Amqp091Queue010")
  def this() = this(InternalAmqp091Queue010())
}

@JSExportAll
case class Amqp091ChannelBinding020(override private[amf] val _internal: InternalAmqp091ChannelBinding020)
    extends Amqp091ChannelBinding(_internal) {

  @JSExportTopLevel("Amqp091ChannelBinding020")
  def this() = this(InternalAmqp091ChannelBinding020())

  override def exchange: Amqp091ChannelExchange020 = _internal.exchange
  override def queue: Amqp091Queue020              = _internal.queue

  def withExchange(exchange: Amqp091ChannelExchange020): this.type = {
    _internal.withExchange(exchange)
    this
  }
  def withQueue(queue: Amqp091Queue020): this.type = {
    _internal.withQueue(queue)
    this
  }

  override def linkCopy(): Amqp091ChannelBinding020 = _internal.linkCopy()
}

@JSExportAll
case class Amqp091ChannelExchange020(override private[amf] val _internal: InternalAmqp091ChannelExchange020)
    extends Amqp091ChannelExchange(_internal) {

  @JSExportTopLevel("Amqp091ChannelExchange020")
  def this() = this(InternalAmqp091ChannelExchange020())

  def vHost: StrField = _internal.vHost

  def withVHost(vHost: String): this.type = {
    _internal.withVHost(vHost)
    this
  }
}

@JSExportAll
case class Amqp091Queue020(override private[amf] val _internal: InternalAmqp091Queue020)
    extends Amqp091Queue(_internal) {

  @JSExportTopLevel("Amqp091Queue020")
  def this() = this(InternalAmqp091Queue020())

  def vHost: StrField = _internal.vHost

  def withVHost(vHost: String): this.type = {
    _internal.withVHost(vHost)
    this
  }
}
