package amf.apicontract.client.platform.model.domain

import amf.core.client.platform.model
import amf.core.client.platform.model.domain.Shape

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class KafkaMessageBinding(override private[amf] val _internal: InternalKafkaMessageBinding)
    extends MessageBinding
    with BindingVersion {
  @JSExportTopLevel("model.domain.KafkaMessageBinding")
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
