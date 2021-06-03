package amf.client.model.domain

import amf.client.convert.ApiClientConverters._
import amf.client.model.StrField

import scala.scalajs.js.annotation.{JSExportTopLevel, JSExportAll}
import amf.plugins.domain.apicontract.models.bindings.amqp.{Amqp091MessageBinding => InternalAmqp091MessageBinding}

@JSExportAll
case class Amqp091MessageBinding(override private[amf] val _internal: InternalAmqp091MessageBinding)
    extends MessageBinding
    with BindingVersion {

  @JSExportTopLevel("model.domain.Amqp091MessageBinding")
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
