package amf.apicontract.client.platform.model.domain.bindings.kafka

import amf.apicontract.client.platform.model.domain.bindings.{BindingVersion, MessageBinding}
import amf.core.client.platform.model
import amf.core.client.platform.model.domain.Shape
import amf.apicontract.internal.convert.ApiClientConverters._
import amf.apicontract.client.scala.model.domain.bindings.kafka.{KafkaMessageBinding => InternalKafkaMessageBinding}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class KafkaMessageBinding(override private[amf] val _internal: InternalKafkaMessageBinding)
    extends MessageBinding
    with BindingVersion {
  @JSExportTopLevel("KafkaMessageBinding")
  def this() = this(InternalKafkaMessageBinding())

  def messageKey: Shape = _internal.messageKey

  def withKey(key: Shape): this.type = {
    _internal.withKey(key)
    this
  }
  override protected def bindingVersion: model.StrField = _internal.bindingVersion

  override def withBindingVersion(bindingVersion: String): this.type = {
    _internal.withBindingVersion(bindingVersion)
    this
  }

  override def linkCopy(): KafkaMessageBinding = _internal.linkCopy()
}
