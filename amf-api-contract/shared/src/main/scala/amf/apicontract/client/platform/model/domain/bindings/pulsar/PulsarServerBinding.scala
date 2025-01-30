package amf.apicontract.client.platform.model.domain.bindings.pulsar

import amf.apicontract.client.platform.model.domain.bindings.{BindingVersion, ServerBinding}
import amf.apicontract.client.scala.model.domain.bindings.pulsar.{PulsarServerBinding => InternalPulsarServerBinding}
import amf.apicontract.internal.convert.ApiClientConverters._
import amf.core.client.platform.model
import amf.core.client.platform.model.StrField

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class PulsarServerBinding(override private[amf] val _internal: InternalPulsarServerBinding)
    extends ServerBinding
    with BindingVersion {
  @JSExportTopLevel("PulsarServerBinding")
  def this() = this(InternalPulsarServerBinding())

  def tenant: StrField = _internal.tenant

  def withTenant(tenant: String): this.type = {
    _internal.withTenant(tenant)
    this
  }

  override protected def bindingVersion: model.StrField = _internal.bindingVersion

  override def withBindingVersion(bindingVersion: String): this.type = {
    _internal.withBindingVersion(bindingVersion)
    this
  }

  override def linkCopy(): PulsarServerBinding = _internal.linkCopy()
}
