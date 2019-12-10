package amf.client.model.domain

import amf.client.convert.WebApiClientConverters._
import amf.client.model.{BoolField, StrField, IntField}

import scala.scalajs.js.annotation.{JSExportTopLevel, JSExportAll}
import amf.plugins.domain.webapi.models.bindings.mqtt.{
  MqttServerBinding => InternalMqttServerBinding,
  MqttServerLastWill => InternalMqttServerLastWill
}

@JSExportAll
case class MqttServerBinding(override private[amf] val _internal: InternalMqttServerBinding)
    extends ServerBinding
    with BindingVersion {
  @JSExportTopLevel("model.domain.MqttServerBinding")
  def this() = this(InternalMqttServerBinding())
  override protected def bindingVersion: StrField = _internal.bindingVersion
  override def withBindingVersion(bindingVersion: String): this.type = {
    _internal.withBindingVersion(bindingVersion)
    this
  }

  def clientId: StrField           = _internal.clientId
  def clientSession: BoolField     = _internal.clientSession
  def lastWill: MqttServerLastWill = _internal.lastWill
  def keepAlive: IntField          = _internal.keepAlive

  def withClientId(clientId: String): this.type = {
    _internal.withClientId(clientId)
    this
  }
  def withClientSession(clientSession: Boolean): this.type = {
    _internal.withClientSession(clientSession)
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
  @JSExportTopLevel("model.domain.MqttServerLastWill")
  def this() = this(InternalMqttServerLastWill())

  def topic: StrField   = _internal.topic
  def qos: IntField     = _internal.qos
  def retain: BoolField = _internal.retain

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
}
