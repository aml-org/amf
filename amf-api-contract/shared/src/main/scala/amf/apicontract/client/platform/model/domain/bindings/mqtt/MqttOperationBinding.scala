package amf.apicontract.client.platform.model.domain.bindings.mqtt

import amf.apicontract.client.platform.model.domain.bindings.{BindingVersion, OperationBinding}
import amf.core.client.platform.model.{BoolField, IntField, StrField}
import amf.apicontract.client.scala.model.domain.bindings.mqtt.{MqttOperationBinding => InternalMqttOperationBinding}
import amf.apicontract.internal.convert.ApiClientConverters._

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
