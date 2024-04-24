package amf.apicontract.client.platform.model.domain.bindings.kafka

import amf.apicontract.client.platform.model.domain.bindings.{BindingVersion, ChannelBinding}
import amf.apicontract.client.scala.model.domain.bindings.kafka.{
  KafkaTopicConfiguration => InternalKafkaTopicConfiguration,
  KafkaChannelBinding => InternalKafkaChannelBinding,
  KafkaChannelBinding030 => InternalKafkaChannelBinding030,
  KafkaChannelBinding040 => InternalKafkaChannelBinding040
}
import amf.apicontract.internal.convert.ApiClientConverters._
import amf.core.client.platform.model.{IntField, StrField}
import amf.core.client.platform.model.domain.DomainElement
import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
abstract class KafkaChannelBinding(override private[amf] val _internal: InternalKafkaChannelBinding)
    extends ChannelBinding
    with BindingVersion {
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
}

@JSExportAll
case class KafkaChannelBinding030(override private[amf] val _internal: InternalKafkaChannelBinding030)
    extends KafkaChannelBinding(_internal) {
  @JSExportTopLevel("KafkaChannelBinding030")
  def this() = this(InternalKafkaChannelBinding030())
  override def linkCopy(): KafkaChannelBinding030 = _internal.linkCopy()
}

@JSExportAll
case class KafkaChannelBinding040(override private[amf] val _internal: InternalKafkaChannelBinding040)
    extends KafkaChannelBinding(_internal) {
  @JSExportTopLevel("KafkaChannelBinding040")
  def this() = this(InternalKafkaChannelBinding040())
  override def linkCopy(): KafkaChannelBinding040 = _internal.linkCopy()

  def topicConfiguration: KafkaTopicConfiguration = _internal.topicConfiguration
  def withTopicConfiguration(topicConfiguration: KafkaTopicConfiguration): this.type = {
    _internal.withTopicConfiguration(topicConfiguration)
    this
  }
}

@JSExportAll
case class KafkaTopicConfiguration(override private[amf] val _internal: InternalKafkaTopicConfiguration)
    extends DomainElement {
  @JSExportTopLevel("KafkaTopicConfiguration")
  def this() = this(InternalKafkaTopicConfiguration())
  def cleanupPolicy: ClientList[StrField] = _internal.cleanupPolicy.asClient
  def retentionMs: IntField               = _internal.retentionMs
  def retentionBytes: IntField            = _internal.retentionBytes
  def deleteRetentionMs: IntField         = _internal.deleteRetentionMs
  def maxMessageBytes: IntField           = _internal.maxMessageBytes

  def withCleanupPolicy(cleanupPolicy: ClientList[String]): this.type = {
    _internal.withCleanupPolicy(cleanupPolicy.asInternal)
    this
  }
  def withRetentionMs(retentionMs: Int): this.type = {
    _internal.withRetentionMs(retentionMs)
    this
  }
  def withRetentionBytes(retentionBytes: Int): this.type = {
    _internal.withRetentionBytes(retentionBytes)
    this
  }
  def withDeleteRetentionMs(deleteRetentionMs: Int): this.type = {
    _internal.withDeleteRetentionMS(deleteRetentionMs)
    this
  }
  def withMaxMessageBytes(maxMessageBytes: Int): this.type = {
    _internal.withMaxMessageBytes(maxMessageBytes)
    this
  }
}
