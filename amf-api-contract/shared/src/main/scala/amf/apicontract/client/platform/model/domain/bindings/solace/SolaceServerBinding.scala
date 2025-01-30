package amf.apicontract.client.platform.model.domain.bindings.solace

import amf.apicontract.client.platform.model.domain.bindings.{BindingVersion, ServerBinding}
import amf.apicontract.client.scala.model.domain.bindings.solace.{
  SolaceServerBinding => InternalSolaceServerBinding,
  SolaceServerBinding010 => InternalSolaceServerBinding010,
  SolaceServerBinding040 => InternalSolaceServerBinding040
}
import amf.apicontract.internal.convert.ApiClientConverters._
import amf.core.client.platform.model
import amf.core.client.platform.model.StrField
import amf.core.client.platform.model.domain.Linkable

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
abstract class SolaceServerBinding(override private[amf] val _internal: InternalSolaceServerBinding)
    extends ServerBinding
    with BindingVersion {

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

}

case class SolaceServerBinding010(override private[amf] val _internal: InternalSolaceServerBinding010)
    extends SolaceServerBinding(_internal) {
  @JSExportTopLevel("InternalSolaceServerBinding010")
  def this() = this(InternalSolaceServerBinding010())
  override def linkCopy(): SolaceServerBinding = _internal.linkCopy()
}
case class SolaceServerBinding040(override private[amf] val _internal: InternalSolaceServerBinding040)
    extends SolaceServerBinding(_internal) {
  @JSExportTopLevel("InternalSolaceServerBinding040")
  def this() = this(InternalSolaceServerBinding040())
  def clientName: StrField = _internal.clientName
  def withClientName(clientName: String): this.type = {
    _internal.withClientName(clientName)
    this
  }
  override def linkCopy(): SolaceServerBinding = _internal.linkCopy()
}
