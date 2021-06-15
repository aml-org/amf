package amf.client.model.domain
import amf.client.convert.ApiClientConverters._

import amf.core.client.platform.model.{StrField, BoolField, IntField}
import amf.plugins.domain.apicontract.models.bindings.amqp.{Amqp091OperationBinding => InternalAmqp091OperationBinding}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class Amqp091OperationBinding(override private[amf] val _internal: InternalAmqp091OperationBinding)
    extends OperationBinding
    with BindingVersion {

  @JSExportTopLevel("model.domain.Amqp091OperationBinding")
  def this() = this(InternalAmqp091OperationBinding())
  override protected def bindingVersion: StrField = _internal.bindingVersion
  override def withBindingVersion(bindingVersion: String): this.type = {
    _internal.withBindingVersion(bindingVersion)
    this
  }

  def expiration: IntField      = _internal.expiration
  def userId: StrField          = _internal.userId
  def cc: ClientList[StrField]  = _internal.cc.asClient
  def priority: IntField        = _internal.priority
  def deliveryMode: IntField    = _internal.deliveryMode
  def mandatory: BoolField      = _internal.mandatory
  def bcc: ClientList[StrField] = _internal.bcc.asClient
  def replyTo: StrField         = _internal.replyTo
  def timestamp: BoolField      = _internal.timestamp
  def ack: BoolField            = _internal.ack

  def withExpiration(expiration: Int): this.type = {
    _internal.withExpiration(expiration)
    this
  }

  def withUserId(userId: String): this.type = {
    _internal.withUserId(userId)
    this
  }

  def withCc(cC: ClientList[String]): this.type = {
    _internal.withCc(cC.asInternal)
    this
  }

  def withPriority(priority: Int): this.type = {
    _internal.withPriority(priority)
    this
  }

  def withDeliveryMode(deliveryMode: Int): this.type = {
    _internal.withDeliveryMode(deliveryMode)
    this
  }

  def withMandatory(mandatory: Boolean): this.type = {
    _internal.withMandatory(mandatory)
    this
  }

  def withBcc(bCC: ClientList[String]): this.type = {
    _internal.withBcc(bCC.asInternal)
    this
  }

  def withReplyTo(replyTo: String): this.type = {
    _internal.withReplyTo(replyTo)
    this
  }

  def withTimestamp(timestamp: Boolean): this.type = {
    _internal.withTimestamp(timestamp)
    this
  }

  def withAck(ack: Boolean): this.type = {
    _internal.withAck(ack)
    this
  }

  override def linkCopy(): Amqp091OperationBinding = _internal.linkCopy()
}
