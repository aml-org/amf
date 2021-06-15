package amf.client.model.domain
import amf.client.convert.ApiClientConverters._
import amf.core.client.platform.model.{BoolField, IntField, StrField}
import amf.plugins.domain.apicontract.models.bindings.mqtt.{MqttOperationBinding => InternalMqttOperationBinding}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class MqttOperationBinding(override private[amf] val _internal: InternalMqttOperationBinding)
    extends OperationBinding
    with BindingVersion {
  @JSExportTopLevel("model.domain.MqttOperationBinding")
  def this() = this(InternalMqttOperationBinding())

  def qos: IntField     = _internal.qos
  def retain: BoolField = _internal.retain

  def withQos(qos: Int): this.type = {
    _internal.withQos(qos)
    this
  }

  def withRetain(retain: Boolean): this.type = {
    _internal.withRetain(retain)
    this
  }

  override protected def bindingVersion: StrField = _internal.bindingVersion

  override def withBindingVersion(bindingVersion: String): this.type = {
    _internal.withBindingVersion(bindingVersion)
    this
  }

  override def linkCopy(): MqttOperationBinding = _internal.linkCopy()

}
