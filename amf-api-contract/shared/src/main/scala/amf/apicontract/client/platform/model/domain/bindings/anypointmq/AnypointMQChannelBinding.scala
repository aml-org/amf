package amf.apicontract.client.platform.model.domain.bindings.anypointmq

import amf.apicontract.client.platform.model.domain.bindings.{BindingVersion, ChannelBinding}
import amf.core.client.platform.model
import amf.apicontract.client.scala.model.domain.bindings.anypointmq.{
  AnypointMQChannelBinding => InternalAnypointMQChannelBinding
}
import amf.core.client.platform.model.StrField
import amf.apicontract.internal.convert.ApiClientConverters._
import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class AnypointMQChannelBinding(override private[amf] val _internal: InternalAnypointMQChannelBinding)
    extends ChannelBinding
    with BindingVersion {
  @JSExportTopLevel("AnypointMQChannelBinding")
  def this() = this(InternalAnypointMQChannelBinding())

  def destination: StrField = _internal.destination

  def withDestination(destination: String): this.type = {
    _internal.withDestination(destination)
    this
  }

  def destinationType: StrField = _internal.destinationType

  def withDestinationType(destinationType: String): this.type = {
    _internal.withDestinationType(destinationType)
    this
  }

  override protected def bindingVersion: model.StrField = _internal.bindingVersion

  override def withBindingVersion(bindingVersion: String): this.type = {
    _internal.withBindingVersion(bindingVersion)
    this
  }

  override def linkCopy(): AnypointMQChannelBinding = _internal.linkCopy()
}
