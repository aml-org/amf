package amf.apicontract.client.platform.model.domain.bindings.kafka

import amf.apicontract.client.platform.model.domain.bindings.{BindingVersion, ChannelBinding}
import amf.apicontract.client.scala.model.domain.bindings.kafka.{KafkaChannelBinding => InternalKafkaChannelBinding}
import amf.apicontract.internal.convert.ApiClientConverters._
import amf.core.client.platform.model.{IntField, StrField}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class KafkaChannelBinding(override private[amf] val _internal: InternalKafkaChannelBinding)
    extends ChannelBinding
    with BindingVersion {
  @JSExportTopLevel("KafkaChannelBinding")
  def this() = this(InternalKafkaChannelBinding())
  override protected def bindingVersion: StrField = _internal.bindingVersion
  override def withBindingVersion(bindingVersion: String): this.type = {
    _internal.withBindingVersion(bindingVersion)
    this
  }

  def topic: StrField      = _internal.topic
  def partitions: IntField = _internal.partitions
  def replicas: IntField   = _internal.replicas

  def withTopic(topic: String): this.type = {
    _internal.withTopic(topic)
    this
  }

  def withPartitions(partitions: Int): this.type = {
    _internal.withPartitions(partitions)
    this
  }

  def withReplicas(replicas: Int): this.type = {
    _internal.withReplicas(replicas)
    this
  }

  override def linkCopy(): KafkaChannelBinding = _internal.linkCopy()

}
