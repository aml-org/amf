package amf.apicontract.client.platform.model.domain.bindings.ibmmq

import amf.apicontract.client.platform.model.domain.bindings.{BindingVersion, MessageBinding}
import amf.apicontract.client.scala.model.domain.bindings.ibmmq.{IBMMQMessageBinding => InternalIBMMQMessageBinding}
import amf.apicontract.internal.convert.ApiClientConverters._
import amf.core.client.platform.model
import amf.core.client.platform.model.{IntField, StrField}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class IBMMQMessageBinding(override private[amf] val _internal: InternalIBMMQMessageBinding)
    extends MessageBinding
    with BindingVersion {
  @JSExportTopLevel("IBMMQMessageBinding")
  def this() = this(InternalIBMMQMessageBinding())

  def messageType: StrField         = _internal.messageType
  def headers: ClientList[StrField] = _internal.headers.asClient
  def description: StrField         = _internal.description
  def expiry: IntField              = _internal.expiry

  def withType(messageType: String): this.type = {
    _internal.withMessageType(messageType)
    this
  }

  def withHeaders(headers: String): this.type = {
    _internal.withHeaders(headers)
    this
  }

  def withDescription(description: String): this.type = {
    _internal.withDescription(description)
    this
  }

  def withExpiry(expiry: Int): this.type = {
    _internal.withExpiry(expiry)
    this
  }

  override protected def bindingVersion: StrField = _internal.bindingVersion

  override def withBindingVersion(bindingVersion: String): this.type = {
    _internal.withBindingVersion(bindingVersion)
    this
  }

  override def linkCopy(): IBMMQMessageBinding = _internal.linkCopy()
}
