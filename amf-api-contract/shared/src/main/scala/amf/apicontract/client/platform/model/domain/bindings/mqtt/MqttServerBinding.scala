package amf.apicontract.client.platform.model.domain.bindings.mqtt

import amf.apicontract.client.platform.model.domain.bindings.{BindingVersion, ServerBinding}
import amf.apicontract.client.scala.model.domain.bindings.mqtt.{
  MqttServerBinding => InternalMqttServerBinding,
  MqttServerBinding010 => InternalMqttServerBinding010,
  MqttServerBinding020 => InternalMqttServerBinding020,
  MqttServerLastWill => InternalMqttServerLastWill
}
import amf.apicontract.internal.convert.ApiClientConverters._
import amf.core.client.platform.model.domain.{DomainElement, Shape}
import amf.core.client.platform.model.{BoolField, IntField, StrField}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
abstract class MqttServerBinding(override private[amf] val _internal: InternalMqttServerBinding)
    extends ServerBinding
    with BindingVersion {
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
}

@JSExportAll
case class MqttServerBinding010(override private[amf] val _internal: InternalMqttServerBinding010)
    extends MqttServerBinding(_internal) {
  @JSExportTopLevel("MqttServerBinding010")
  def this() = this(InternalMqttServerBinding010())
  override def linkCopy(): MqttServerBinding010 = _internal.linkCopy()
}

@JSExportAll
case class MqttServerBinding020(override private[amf] val _internal: InternalMqttServerBinding020)
    extends MqttServerBinding(_internal) {
  @JSExportTopLevel("MqttServerBinding020")
  def this() = this(InternalMqttServerBinding020())

  def sessionExpiryInterval: IntField    = _internal.sessionExpiryInterval
  def sessionExpiryIntervalSchema: Shape = _internal.sessionExpiryIntervalSchema

  def maximumPacketSize: IntField    = _internal.maximumPacketSize
  def maximumPacketSizeSchema: Shape = _internal.maximumPacketSizeSchema

  def withSessionExpiryInterval(sessionExpiryInterval: Int): this.type = {
    _internal.withSessionExpiryInterval(sessionExpiryInterval)
    this
  }
  def withSessionExpiryIntervalSchema(sessionExpiryIntervalSchema: Shape): this.type = {
    _internal.withSessionExpiryIntervalSchema(sessionExpiryIntervalSchema)
    this
  }

  def withMaximumPacketSize(maximumPacketSize: Int): this.type = {
    _internal.withMaximumPacketSize(maximumPacketSize)
    this
  }
  def withMaximumPacketSizeSchema(maximumPacketSizeSchema: Shape): this.type = {
    _internal.withMaximumPacketSizeSchema(maximumPacketSizeSchema)
    this
  }

  override def linkCopy(): MqttServerBinding020 = _internal.linkCopy()
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
