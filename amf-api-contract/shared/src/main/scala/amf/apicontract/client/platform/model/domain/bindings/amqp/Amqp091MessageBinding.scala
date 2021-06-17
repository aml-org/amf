package amf.apicontract.client.platform.model.domain.bindings.amqp

import amf.apicontract.client.platform.model.domain.bindings.{BindingVersion, MessageBinding}
import amf.core.client.platform.model.StrField
import amf.apicontract.client.scala.model.domain.bindings.amqp.{Amqp091MessageBinding => InternalAmqp091MessageBinding}
import amf.apicontract.internal.convert.ApiClientConverters._

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class Amqp091MessageBinding(override private[amf] val _internal: InternalAmqp091MessageBinding)
    extends MessageBinding
    with BindingVersion {

  @JSExportTopLevel("Amqp091MessageBinding")
  def this() = this(InternalAmqp091MessageBinding())
  override protected def bindingVersion: StrField = _internal.bindingVersion

  override def withBindingVersion(bindingVersion: String): this.type = {
    _internal.withBindingVersion(bindingVersion)
    this
  }

  def contentEncoding: StrField = _internal.contentEncoding
  def messageType: StrField     = _internal.messageType

  def withContentEncoding(contentEncoding: String): this.type = {
    _internal.withContentEncoding(contentEncoding)
    this
  }

  def withMessageType(messageType: String): this.type = {
    _internal.withMessageType(messageType)
    this
  }

  override def linkCopy(): Amqp091MessageBinding = _internal.linkCopy()
}
