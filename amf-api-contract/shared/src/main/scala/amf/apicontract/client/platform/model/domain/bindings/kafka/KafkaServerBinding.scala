package amf.apicontract.client.platform.model.domain.bindings.kafka

import amf.apicontract.client.platform.model.domain.bindings.{BindingVersion, ServerBinding}
import amf.apicontract.client.scala.model.domain.bindings.kafka.{KafkaServerBinding => InternalKafkaServerBinding}
import amf.apicontract.internal.convert.ApiClientConverters._
import amf.core.client.platform.model.StrField

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class KafkaServerBinding(override private[amf] val _internal: InternalKafkaServerBinding)
    extends ServerBinding
    with BindingVersion {
  @JSExportTopLevel("KafkaServerBinding")
  def this() = this(InternalKafkaServerBinding())
  override protected def bindingVersion: StrField = _internal.bindingVersion
  override def withBindingVersion(bindingVersion: String): this.type = {
    _internal.withBindingVersion(bindingVersion)
    this
  }

  def schemaRegistryUrl: StrField    = _internal.schemaRegistryUrl
  def schemaRegistryVendor: StrField = _internal.schemaRegistryVendor

  def withSchemaRegistryUrl(schemaRegistryUrl: String): this.type = {
    _internal.withSchemaRegistryUrl(schemaRegistryUrl)
    this
  }

  def withSchemaRegistryVendor(schemaRegistryVendor: String): this.type = {
    _internal.withSchemaRegistryVendor(schemaRegistryVendor)
    this
  }

  override def linkCopy(): KafkaServerBinding = _internal.linkCopy()

}
