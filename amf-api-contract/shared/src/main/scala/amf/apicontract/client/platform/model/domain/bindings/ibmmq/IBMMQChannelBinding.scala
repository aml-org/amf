package amf.apicontract.client.platform.model.domain.bindings.ibmmq

import amf.apicontract.client.platform.model.domain.bindings.{BindingVersion, ChannelBinding}
import amf.apicontract.client.scala.model.domain.bindings.ibmmq.{
  IBMMQChannelTopic => InternalIBMMQChannelTopic,
  IBMMQChannelBinding => InternalIBMMQChannelBinding,
  IBMMQChannelQueue => InternalIBMMQChannelQueue
}
import amf.apicontract.internal.convert.ApiClientConverters._
import amf.core.client.platform.model
import amf.core.client.platform.model.domain.{DomainElement, NamedDomainElement}
import amf.core.client.platform.model.{BoolField, IntField, StrField}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class IBMMQChannelBinding(override private[amf] val _internal: InternalIBMMQChannelBinding)
    extends ChannelBinding
    with BindingVersion {
  @JSExportTopLevel("IBMMQChannelBinding")
  def this() = this(InternalIBMMQChannelBinding())

  def destinationType: StrField = _internal.destinationType
  def queue: IBMMQChannelQueue  = _internal.queue
  def topic: IBMMQChannelTopic  = _internal.topic
  def maxMsgLength: IntField    = _internal.maxMsgLength

  def withDestinationType(destinationType: String): this.type = {
    _internal.withDestinationType(destinationType)
    this
  }

  def withQueue(queue: IBMMQChannelQueue): this.type = {
    _internal.withQueue(queue)
    this
  }

  def withTopic(topic: IBMMQChannelTopic): this.type = {
    _internal.withTopic(topic)
    this
  }

  def withMaxMsgLength(maxMsgLength: Int): this.type = {
    _internal.withMaxMsgLength(maxMsgLength)
    this
  }

  override protected def bindingVersion: model.StrField = _internal.bindingVersion

  override def withBindingVersion(bindingVersion: String): this.type = {
    _internal.withBindingVersion(bindingVersion)
    this
  }

  override def linkCopy(): IBMMQChannelBinding = _internal.linkCopy()
}

@JSExportAll
case class IBMMQChannelQueue(override private[amf] val _internal: InternalIBMMQChannelQueue)
    extends DomainElement
    with NamedDomainElement {

  @JSExportTopLevel("IBMMQChannelQueue")
  def this() = this(InternalIBMMQChannelQueue())

  def objectName: StrField     = _internal.objectName
  def isPartitioned: BoolField = _internal.isPartitioned
  def exclusive: BoolField     = _internal.exclusive

  def withObjectName(objectName: String): this.type = {
    _internal.withObjectName(objectName)
    this
  }

  def withIsPartitioned(isPartitioned: Boolean): this.type = {
    _internal.withIsPartitioned(isPartitioned)
    this
  }

  def withExclusive(exclusive: Boolean): this.type = {
    _internal.withExclusive(exclusive)
    this
  }

  override def name: StrField = _internal.name

  override def withName(name: String): this.type = {
    _internal.withName(name)
    this
  }
}

@JSExportAll
case class IBMMQChannelTopic(override private[amf] val _internal: InternalIBMMQChannelTopic)
    extends DomainElement
    with NamedDomainElement {

  @JSExportTopLevel("IBMMQChannelTopic")
  def this() = this(InternalIBMMQChannelTopic())

  def string: StrField = _internal.string
  def objectName: StrField        = _internal.objectName
  def durablePermitted: BoolField = _internal.durablePermitted
  def lastMsgRetained: BoolField  = _internal.lastMsgRetained

  def withString(string: String): this.type = {
    _internal.withString(string)
    this
  }

  def withObjectName(objectName: String): this.type = {
    _internal.withObjectName(objectName)
    this
  }

  def withDurablePermitted(durablePermitted: Boolean): this.type = {
    _internal.withDurablePermitted(durablePermitted)
    this
  }

  def withLastMsgRetained(lastMsgRetained: Boolean): this.type = {
    _internal.withLastMsgRetained(lastMsgRetained)
    this
  }

  override def name: StrField = _internal.name

  override def withName(name: String): this.type = {
    _internal.withName(name)
    this
  }
}
