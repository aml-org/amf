package amf.apicontract.client.platform.model.domain.bindings.mqtt

import amf.apicontract.client.platform.model.domain.bindings.{BindingVersion, ServerBinding}
import amf.apicontract.client.scala.model.domain.bindings.mqtt.{
  MqttServerBinding => InternalMqttServerBinding,
  MqttServerLastWill => InternalMqttServerLastWill
}
import amf.apicontract.internal.convert.ApiClientConverters._
import amf.core.client.platform.model.domain.DomainElement
import amf.core.client.platform.model.{BoolField, IntField, StrField}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class MqttServerBinding(override private[amf] val _internal: InternalMqttServerBinding)
    extends ServerBinding
    with BindingVersion {
  @JSExportTopLevel("MqttServerBinding")
  def this() = this(InternalMqttServerBinding())
  override protected def bindingVersion: StrField = _internal.bindingVersion
  override def withBindingVersion(bindingVersion: String): this.type = {
    _internal.withBindingVersion(bindingVersion)
    this
  }

  def clientId: StrField           = _internal.clientId
  def cleanSession: BoolField      = _internal.cleanSession
  def lastWill: MqttServerLastWill = _internal.lastWill
  def keepAlive: IntField          = _internal.keepAlive

  def withClientId(clientId: String): this.type = {
    _internal.withClientId(clientId)
    this
  }
  def withCleanSession(cleanSession: Boolean): this.type = {
    _internal.withCleanSession(cleanSession)
    this
  }
  def withLastWill(lastWill: MqttServerLastWill): this.type = {
    _internal.withLastWill(lastWill)
    this
  }
  def withKeepAlive(keepAlive: Int): this.type = {
    _internal.withKeepAlive(keepAlive)
    this
  }

  override def linkCopy(): MqttServerBinding = _internal.linkCopy()
}

@JSExportAll
case class MqttServerLastWill(override private[amf] val _internal: InternalMqttServerLastWill) extends DomainElement {
  @JSExportTopLevel("MqttServerLastWill")
  def this() = this(InternalMqttServerLastWill())

  def topic: StrField   = _internal.topic
  def qos: IntField     = _internal.qos
  def retain: BoolField = _internal.retain
  def message: StrField = _internal.message

  def withTopic(topic: String): this.type = {
    _internal.withTopic(topic)
    this
  }
  def withQos(qos: Int): this.type = {
    _internal.withQos(qos)
    this
  }
  def withRetain(retain: Boolean): this.type = {
    _internal.withRetain(retain)
    this
  }
  def withMessage(message: String): this.type = {
    _internal.withMessage(message)
    this
  }
}
