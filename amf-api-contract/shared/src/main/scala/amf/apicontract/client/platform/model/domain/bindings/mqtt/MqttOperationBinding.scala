package amf.apicontract.client.platform.model.domain.bindings.mqtt

import amf.apicontract.client.platform.model.domain.bindings.{BindingVersion, OperationBinding}
import amf.core.client.platform.model.{BoolField, IntField, StrField}
import amf.apicontract.client.scala.model.domain.bindings.mqtt.{
  MqttOperationBinding => InternalMqttOperationBinding,
  MqttOperationBinding010 => InternalMqttOperationBinding010,
  MqttOperationBinding020 => InternalMqttOperationBinding020
}
import amf.apicontract.internal.convert.ApiClientConverters._
import amf.core.client.platform.model.domain.Shape

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
abstract class MqttOperationBinding(override private[amf] val _internal: InternalMqttOperationBinding)
    extends OperationBinding
    with BindingVersion {
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
}

@JSExportAll
case class MqttOperationBinding010(override private[amf] val _internal: InternalMqttOperationBinding010)
    extends MqttOperationBinding(_internal) {
  @JSExportTopLevel("MqttOperationBinding010")
  def this() = this(InternalMqttOperationBinding010())
  override def linkCopy(): MqttOperationBinding010 = _internal.linkCopy()
}

@JSExportAll
case class MqttOperationBinding020(override private[amf] val _internal: InternalMqttOperationBinding020)
    extends MqttOperationBinding(_internal) {
  @JSExportTopLevel("MqttOperationBinding020")
  def this() = this(InternalMqttOperationBinding020())

  def messageExpiryInterval: IntField    = _internal.messageExpiryInterval
  def messageExpiryIntervalSchema: Shape = _internal.messageExpiryIntervalSchema

  def withMessageExpiryInterval(messageExpiryInterval: Int): this.type = {
    _internal.withMessageExpiryInterval(messageExpiryInterval)
    this
  }

  def withMessageExpiryIntervalSchema(messageExpiryIntervalSchema: Shape): this.type = {
    _internal.withMessageExpiryIntervalSchema(messageExpiryIntervalSchema)
    this
  }

  override def linkCopy(): MqttOperationBinding020 = _internal.linkCopy()
}
