package amf.client.model.domain

import amf.client.convert.WebApiClientConverters._
import amf.client.model
import amf.core.model.StrField
import amf.plugins.domain.webapi.metamodel.bindings.KafkaMessageBindingModel.Key

import scala.scalajs.js.annotation.{JSExportTopLevel, JSExportAll}
import amf.plugins.domain.webapi.models.bindings.kafka.{KafkaMessageBinding => InternalKafkaMessageBinding}

@JSExportAll
class KafkaMessageBinding(override private[amf] val _internal: InternalKafkaMessageBinding) extends MessageBinding with BindingVersion {
  @JSExportTopLevel("model.domain.KafkaMessageBinding")
  def this() = this(InternalKafkaMessageBinding())

  def key: StrField                   = _internal.key

  def withKey(key: String): this.type = {
    _internal.withKey(key)
    this
  }
  override protected def bindingVersion: model.StrField = _internal.bindingVersion

  override def withBindingVersion(bindingVersion: String): this.type = {
    withBindingVersion(bindingVersion)
    this
  }

  override def linkCopy(): KafkaMessageBinding = _internal.linkCopy()
}
