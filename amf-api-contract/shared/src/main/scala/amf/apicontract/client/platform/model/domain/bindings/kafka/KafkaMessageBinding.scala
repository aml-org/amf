package amf.apicontract.client.platform.model.domain.bindings.kafka

import amf.apicontract.client.platform.model.domain.bindings.{BindingVersion, MessageBinding}
import amf.core.client.platform.model
import amf.core.client.platform.model.domain.Shape
import amf.core.client.platform.model.StrField
import amf.apicontract.internal.convert.ApiClientConverters._
import amf.apicontract.client.scala.model.domain.bindings.kafka.{
  KafkaMessageBinding => InternalKafkaMessageBinding,
  KafkaMessageBinding010 => InternalKafkaMessageBinding010,
  KafkaMessageBinding030 => InternalKafkaMessageBinding030
}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
abstract class KafkaMessageBinding(override private[amf] val _internal: InternalKafkaMessageBinding)
    extends MessageBinding
    with BindingVersion {
  override protected def bindingVersion: model.StrField = _internal.bindingVersion
  override def withBindingVersion(bindingVersion: String): this.type = {
    _internal.withBindingVersion(bindingVersion)
    this
  }

  def messageKey: Shape = _internal.messageKey
  def withKey(key: Shape): this.type = {
    _internal.withKey(key)
    this
  }
}

@JSExportAll
case class KafkaMessageBinding010(override private[amf] val _internal: InternalKafkaMessageBinding010)
    extends KafkaMessageBinding(_internal) {
  @JSExportTopLevel("InternalKafkaMessageBinding010")
  def this() = this(InternalKafkaMessageBinding010())

  override def linkCopy(): KafkaMessageBinding = _internal.linkCopy()
}

@JSExportAll
case class KafkaMessageBinding030(override private[amf] val _internal: InternalKafkaMessageBinding030)
    extends KafkaMessageBinding(_internal) {
  @JSExportTopLevel("InternalKafkaMessageBinding030")
  def this() = this(InternalKafkaMessageBinding030())

  def schemaIdLocation: StrField        = _internal.schemaIdLocation
  def schemaIdPayloadEncoding: StrField = _internal.schemaIdPayloadEncoding
  def schemaLookupStrategy: StrField    = _internal.schemaLookupStrategy

  def withSchemaIdLocation(schemaIdLocation: String): this.type = {
    _internal.withSchemaIdLocation(schemaIdLocation)
    this
  }

  def withSchemaIdPayloadEncoding(schemaIdPayloadEncoding: String): this.type = {
    _internal.withSchemaIdPayloadEncoding(schemaIdPayloadEncoding)
    this
  }

  def withSchemaLookupStrategy(schemaLookupStrategy: String): this.type = {
    _internal.withSchemaLookupStrategy(schemaLookupStrategy)
    this
  }

  override def linkCopy(): KafkaMessageBinding = _internal.linkCopy()
}
