package amf.apicontract.client.platform.model.domain.bindings.kafka

import amf.apicontract.client.platform.model.domain.bindings.{BindingVersion, ChannelBinding}
import amf.apicontract.client.scala.model.domain.bindings.kafka.{
  KafkaChannelBinding => InternalKafkaChannelBinding,
  KafkaChannelBinding030 => InternalKafkaChannelBinding030,
  KafkaChannelBinding040 => InternalKafkaChannelBinding040,
  KafkaChannelBinding050 => InternalKafkaChannelBinding050,
  KafkaTopicConfiguration => InternalKafkaTopicConfiguration,
  KafkaTopicConfiguration040 => InternalKafkaTopicConfiguration040,
  KafkaTopicConfiguration050 => InternalKafkaTopicConfiguration050
}
import amf.apicontract.internal.convert.ApiClientConverters._
import amf.core.client.platform.model.{IntField, StrField, BoolField}
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

  def topicConfiguration: KafkaTopicConfiguration040 = _internal.topicConfiguration
  def withTopicConfiguration(topicConfiguration: KafkaTopicConfiguration040): this.type = {
    _internal.withTopicConfiguration(topicConfiguration)
    this
  }
}

@JSExportAll
case class KafkaChannelBinding050(override private[amf] val _internal: InternalKafkaChannelBinding050)
    extends KafkaChannelBinding(_internal) {
  @JSExportTopLevel("KafkaChannelBinding050")
  def this() = this(InternalKafkaChannelBinding050())
  override def linkCopy(): KafkaChannelBinding050 = _internal.linkCopy()

  def topicConfiguration: KafkaTopicConfiguration050 = _internal.topicConfiguration
  def withTopicConfiguration(topicConfiguration: KafkaTopicConfiguration050): this.type = {
    _internal.withTopicConfiguration(topicConfiguration)
    this
  }
}

@JSExportAll
abstract class KafkaTopicConfiguration(override private[amf] val _internal: InternalKafkaTopicConfiguration)
    extends DomainElement {
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

@JSExportAll
case class KafkaTopicConfiguration040(override private[amf] val _internal: InternalKafkaTopicConfiguration040)
    extends KafkaTopicConfiguration(_internal) {
  @JSExportTopLevel("KafkaTopicConfiguration040")
  def this() = this(InternalKafkaTopicConfiguration040())
}

@JSExportAll
case class KafkaTopicConfiguration050(override private[amf] val _internal: InternalKafkaTopicConfiguration050)
    extends KafkaTopicConfiguration(_internal) {
  @JSExportTopLevel("KafkaTopicConfiguration050")
  def this() = this(InternalKafkaTopicConfiguration050())

  def confluentKeySchemaValidation: BoolField     = _internal.confluentKeySchemaValidation
  def confluentKeySubjectNameStrategy: StrField   = _internal.confluentKeySubjectNameStrategy
  def confluentValueSchemaValidation: BoolField   = _internal.confluentValueSchemaValidation
  def confluentValueSubjectNameStrategy: StrField = _internal.confluentValueSubjectNameStrategy

  def withConfluentKeySchemaValidation(confluentKeySchemaValidation: Boolean): this.type = {
    _internal.withConfluentKeySchemaValidation(confluentKeySchemaValidation)
    this
  }
  def withConfluentKeySubjectNameStrategy(confluentKeySubjectNameStrategy: String): this.type = {
    _internal.withConfluentKeySubjectNameStrategy(confluentKeySubjectNameStrategy)
    this
  }
  def withConfluentValueSchemaValidation(confluentValueSchemaValidation: Boolean): this.type = {
    _internal.withConfluentValueSchemaValidation(confluentValueSchemaValidation)
    this
  }
  def withConfluentValueSubjectNameStrategy(confluentValueSubjectNameStrategy: String): this.type = {
    _internal.withConfluentValueSubjectNameStrategy(confluentValueSubjectNameStrategy)
    this
  }
}
