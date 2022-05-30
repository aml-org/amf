package amf.apicontract.client.platform.model.domain.bindings.kafka

import amf.apicontract.client.platform.model.domain.bindings.{BindingVersion, OperationBinding}
import amf.core.client.platform.model.StrField
import amf.core.client.platform.model.domain.Shape
import amf.apicontract.internal.convert.ApiClientConverters._
import amf.apicontract.client.scala.model.domain.bindings.kafka.{KafkaOperationBinding => InternalKafkaOperationBinding}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class KafkaOperationBinding(override private[amf] val _internal: InternalKafkaOperationBinding)
    extends OperationBinding
    with BindingVersion {
  @JSExportTopLevel("KafkaOperationBinding")
  def this() = this(InternalKafkaOperationBinding())
  override protected def bindingVersion: StrField = _internal.bindingVersion
  override def withBindingVersion(bindingVersion: String): this.type = {
    _internal.withBindingVersion(bindingVersion)
    this
  }

  def groupId: Shape  = _internal.groupId
  def clientId: Shape = _internal.clientId

  def withGroupId(groupId: Shape): this.type = {
    _internal.withGroupId(groupId)
    this
  }

  def withClientId(clientId: Shape): this.type = {
    _internal.withClientId(clientId)
    this
  }

  override def linkCopy(): KafkaOperationBinding = _internal.linkCopy()

}
