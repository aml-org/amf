package amf.apicontract.client.platform.model.domain.bindings.solace

import amf.apicontract.client.platform.model.domain.bindings.{BindingVersion, ServerBinding}
import amf.apicontract.client.scala.model.domain.bindings.solace.{SolaceServerBinding => InternalSolaceServerBinding}
import amf.apicontract.internal.convert.ApiClientConverters._
import amf.core.client.platform.model
import amf.core.client.platform.model.StrField

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class SolaceServerBinding(override private[amf] val _internal: InternalSolaceServerBinding)
    extends ServerBinding
    with BindingVersion {
  @JSExportTopLevel("SolaceServerBinding")
  def this() = this(InternalSolaceServerBinding())

  def msgVpn: StrField = _internal.msgVpn

  def withMsgVpn(msgVpn: String): this.type = {
    _internal.withMsgVpn(msgVpn)
    this
  }

  override protected def bindingVersion: model.StrField = _internal.bindingVersion

  override def withBindingVersion(bindingVersion: String): this.type = {
    _internal.withBindingVersion(bindingVersion)
    this
  }

  override def linkCopy(): SolaceServerBinding = _internal.linkCopy()
}
