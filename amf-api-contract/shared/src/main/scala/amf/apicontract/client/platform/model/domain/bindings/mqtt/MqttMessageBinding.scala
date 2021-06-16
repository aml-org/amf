package amf.apicontract.client.platform.model.domain.bindings.mqtt

import amf.apicontract.client.platform.model.domain.bindings.{BindingVersion, MessageBinding}
import amf.core.client.platform.model.StrField
import amf.apicontract.internal.convert.ApiClientConverters._
import amf.apicontract.client.scala.model.domain.bindings.mqtt.{MqttMessageBinding => InternalMqttMessageBinding}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class MqttMessageBinding(override private[amf] val _internal: InternalMqttMessageBinding)
    extends MessageBinding
    with BindingVersion {
  @JSExportTopLevel("model.domain.MqttMessageBinding")
  def this() = this(InternalMqttMessageBinding())

  override protected def bindingVersion: StrField = _internal.bindingVersion
  override def withBindingVersion(bindingVersion: String): this.type = {
    _internal.withBindingVersion(bindingVersion)
    this
  }

  override def linkCopy(): MqttMessageBinding = _internal.linkCopy()
}
