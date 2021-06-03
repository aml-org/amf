package amf.client.model.domain
import amf.client.convert.ApiClientConverters._

import amf.client.model.StrField
import amf.core.model.{BoolField, IntField}
import amf.plugins.domain.apicontract.metamodel.bindings.MqttOperationBindingModel.{Qos, Retain}

import scala.scalajs.js.annotation.{JSExportTopLevel, JSExportAll}
import amf.plugins.domain.apicontract.models.bindings.mqtt.{MqttOperationBinding => InternalMqttOperationBinding}

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
