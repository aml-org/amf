package amf.apicontract.client.platform.model.domain

import amf.apicontract.internal.convert.ApiClientConverters._
import amf.core.client.platform.model.domain.{DomainElement, NamedDomainElement}
import amf.core.client.platform.model.{BoolField, StrField}
import amf.apicontract.client.scala.model.domain.bindings.amqp.{Amqp091ChannelBinding => InternalAmqp091ChannelBinding, Amqp091ChannelExchange => InternalAmqp091ChannelExchange, Amqp091Queue => InternalAmqp091Queue}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class Amqp091ChannelBinding(override private[amf] val _internal: InternalAmqp091ChannelBinding)
    extends ChannelBinding
    with BindingVersion {

  @JSExportTopLevel("model.domain.Amqp091ChannelBinding")
  def this() = this(InternalAmqp091ChannelBinding())

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

  override def linkCopy(): Amqp091ChannelBinding = _internal.linkCopy()
}

@JSExportAll
case class Amqp091ChannelExchange(override private[amf] val _internal: InternalAmqp091ChannelExchange)
    extends DomainElement
    with NamedDomainElement {

  @JSExportTopLevel("model.domain.Amqp091ChannelExchange")
  def this() = this(InternalAmqp091ChannelExchange())

  def `type`: StrField      = _internal.`type`
  def durable: BoolField    = _internal.durable
  def autoDelete: BoolField = _internal.autoDelete
  def vHost: StrField       = _internal.vHost

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
  def withVHost(vHost: String): this.type = {
    _internal.withVHost(vHost)
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
case class Amqp091Queue(override private[amf] val _internal: InternalAmqp091Queue)
    extends DomainElement
    with NamedDomainElement {

  @JSExportTopLevel("model.domain.Amqp091Queue")
  def this() = this(InternalAmqp091Queue())

  def durable: BoolField    = _internal.durable
  def exclusive: BoolField  = _internal.exclusive
  def autoDelete: BoolField = _internal.autoDelete
  def vHost: StrField       = _internal.vHost

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
  def withVHost(vHost: String): this.type = {
    _internal.withVHost(vHost)
    this
  }

  override def name: StrField = _internal.name

  override def withName(name: String): this.type = {
    _internal.withName(name)
    this
  }
}
