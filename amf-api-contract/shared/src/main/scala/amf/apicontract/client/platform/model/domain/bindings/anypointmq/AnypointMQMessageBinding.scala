package amf.apicontract.client.platform.model.domain.bindings.anypointmq

import amf.apicontract.client.platform.model.domain.bindings.{BindingHeaders, BindingVersion, MessageBinding}
import amf.apicontract.client.scala.model.domain.bindings.anypointmq.{
  AnypointMQMessageBinding => InternalAnypointMQMessageBinding
}
import amf.apicontract.internal.convert.ApiClientConverters._
import amf.core.client.platform.model
import amf.core.client.platform.model.domain.Shape

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class AnypointMQMessageBinding(override private[amf] val _internal: InternalAnypointMQMessageBinding)
    extends MessageBinding
    with BindingVersion
    with BindingHeaders {
  @JSExportTopLevel("AnypointMQMessageBinding")
  def this() = this(InternalAnypointMQMessageBinding())

  override def headers: Shape = _internal.headers

  override def withHeaders(headers: Shape): this.type = {
    _internal.withHeaders(headers)
    this
  }

  override protected def bindingVersion: model.StrField = _internal.bindingVersion

  override def withBindingVersion(bindingVersion: String): this.type = {
    _internal.withBindingVersion(bindingVersion)
    this
  }

  override def linkCopy(): AnypointMQMessageBinding = _internal.linkCopy()
}
