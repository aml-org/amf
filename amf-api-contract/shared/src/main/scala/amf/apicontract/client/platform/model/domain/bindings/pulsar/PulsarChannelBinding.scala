package amf.apicontract.client.platform.model.domain.bindings.pulsar

import amf.apicontract.client.platform.model.domain.bindings.{BindingVersion, ChannelBinding}
import amf.apicontract.client.scala.model.domain.bindings.pulsar.{
  PulsarChannelBinding => InternalPulsarChannelBinding,
  PulsarChannelRetention => InternalPulsarChannelRetention
}
import amf.apicontract.internal.convert.ApiClientConverters._
import amf.core.client.platform.model
import amf.core.client.platform.model.domain.DomainElement
import amf.core.client.platform.model.{IntField, StrField, BoolField}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class PulsarChannelBinding(override private[amf] val _internal: InternalPulsarChannelBinding)
    extends ChannelBinding
    with BindingVersion {
  @JSExportTopLevel("PulsarChannelBinding")
  def this() = this(InternalPulsarChannelBinding())

  def namespace: StrField                  = _internal.namespace
  def persistence: StrField                = _internal.persistence
  def compaction: IntField                 = _internal.compaction
  def geoReplication: ClientList[StrField] = _internal.geoReplication.asClient
  def retention: PulsarChannelRetention    = _internal.retention
  def ttl: IntField                        = _internal.ttl
  def deduplication: BoolField             = _internal.deduplication

  def withNamespace(namespace: String): this.type = {
    _internal.withNamespace(namespace)
    this
  }

  def withPersistence(persistence: String): this.type = {
    _internal.withPersistence(persistence)
    this
  }

  def withCompaction(compaction: Integer): this.type = {
    _internal.withCompaction(compaction)
    this
  }

  def withGeoReplication(geoReplication: ClientList[String]): this.type = {
    _internal.withGeoReplication(geoReplication.asInternal)
    this
  }

  def withRetention(retention: PulsarChannelRetention): this.type = {
    _internal.withRetention(retention)
    this
  }

  def withTtl(ttl: Integer): this.type = {
    _internal.withTtl(ttl)
    this
  }

  def withDeduplication(deduplication: Boolean): this.type = {
    _internal.withDeduplication(deduplication)
    this
  }

  override protected def bindingVersion: model.StrField = _internal.bindingVersion

  override def withBindingVersion(bindingVersion: String): this.type = {
    _internal.withBindingVersion(bindingVersion)
    this
  }

  override def linkCopy(): PulsarChannelBinding = _internal.linkCopy()
}

@JSExportAll
case class PulsarChannelRetention(override private[amf] val _internal: InternalPulsarChannelRetention)
    extends DomainElement {

  @JSExportTopLevel("PulsarChannelRetention")
  def this() = this(InternalPulsarChannelRetention())

  def time: IntField = _internal.time
  def size: IntField = _internal.size

  def withTime(time: Integer): this.type = {
    _internal.withTime(time)
    this
  }
  def withSize(size: Integer): this.type = {
    _internal.withSize(size)
    this
  }
}
