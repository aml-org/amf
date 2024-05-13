package amf.apicontract.client.platform.model.domain.bindings.mqtt

import amf.apicontract.client.platform.model.domain.bindings.{BindingVersion, MessageBinding}
import amf.core.client.platform.model.{IntField, StrField}
import amf.apicontract.internal.convert.ApiClientConverters._
import amf.apicontract.client.scala.model.domain.bindings.mqtt.{
  MqttMessageBinding => InternalMqttMessageBinding,
  MqttMessageBinding010 => InternalMqttMessageBinding010,
  MqttMessageBinding020 => InternalMqttMessageBinding020
}
import amf.core.client.platform.model.domain.Shape

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
abstract class MqttMessageBinding(override private[amf] val _internal: InternalMqttMessageBinding)
    extends MessageBinding
    with BindingVersion {
  override protected def bindingVersion: StrField = _internal.bindingVersion
  override def withBindingVersion(bindingVersion: String): this.type = {
    _internal.withBindingVersion(bindingVersion)
    this
  }
}

@JSExportAll
case class MqttMessageBinding010(override private[amf] val _internal: InternalMqttMessageBinding010)
    extends MqttMessageBinding(_internal) {
  @JSExportTopLevel("MqttMessageBinding010")
  def this() = this(InternalMqttMessageBinding010())
  override def linkCopy(): MqttMessageBinding010 = _internal.linkCopy()
}

@JSExportAll
case class MqttMessageBinding020(override private[amf] val _internal: InternalMqttMessageBinding020)
    extends MqttMessageBinding(_internal) {
  @JSExportTopLevel("MqttMessageBinding020")
  def this() = this(InternalMqttMessageBinding020())

  def payloadFormatIndicator: IntField = _internal.payloadFormatIndicator
  def correlationData: Shape           = _internal.correlationData
  def contentType: StrField            = _internal.contentType
  def responseTopic: StrField          = _internal.responseTopic

  def withPayloadFormatIndicator(payloadFormatIndicator: Int): this.type = {
    _internal.withPayloadFormatIndicator(payloadFormatIndicator)
    this
  }

  def withCorrelationData(correlationData: Shape): this.type = {
    _internal.withCorrelationData(correlationData)
    this
  }

  def withContentType(contentType: String): this.type = {
    _internal.withContentType(contentType)
    this
  }

  def withResponseTopic(responseTopic: String): this.type = {
    _internal.withResponseTopic(responseTopic)
    this
  }

  override def linkCopy(): MqttMessageBinding020 = _internal.linkCopy()
}
