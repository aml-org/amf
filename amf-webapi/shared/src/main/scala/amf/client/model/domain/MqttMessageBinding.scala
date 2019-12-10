package amf.client.model.domain

import amf.client.convert.WebApiClientConverters._
import amf.client.model.StrField

import scala.scalajs.js.annotation.{JSExportTopLevel, JSExportAll}
import amf.plugins.domain.webapi.models.bindings.mqtt.{MqttMessageBinding => InternalMqttMessageBinding}

@JSExportAll
class MqttMessageBinding(override private[amf] val _internal: InternalMqttMessageBinding) extends MessageBinding with BindingVersion {
  @JSExportTopLevel("model.domain.MqttMessageBinding")
  def this() = this(InternalMqttMessageBinding())

  override protected def bindingVersion: StrField = _internal.bindingVersion
  override def withBindingVersion(bindingVersion: String): this.type = {
    _internal.withBindingVersion(bindingVersion)
    this
  }

  override def linkCopy(): MqttMessageBinding = _internal.linkCopy()
}
