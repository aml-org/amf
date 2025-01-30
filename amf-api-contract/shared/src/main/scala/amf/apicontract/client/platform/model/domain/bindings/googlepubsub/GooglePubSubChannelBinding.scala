package amf.apicontract.client.platform.model.domain.bindings.googlepubsub

import amf.apicontract.client.platform.model.domain.bindings.{BindingVersion, ChannelBinding}
import amf.apicontract.client.scala.model.domain.bindings.googlepubsub.{
  GooglePubSubChannelBinding => InternalGooglePubSubChannelBinding,
  GooglePubSubChannelBinding010 => InternalGooglePubSubChannelBinding010,
  GooglePubSubChannelBinding020 => InternalGooglePubSubChannelBinding020,
  GooglePubSubMessageStoragePolicy => InternalGooglePubSubMessageStoragePolicy,
  GooglePubSubSchemaSettings => InternalGooglePubSubSchemaSettings
}
import amf.apicontract.internal.convert.ApiClientConverters._
import amf.core.client.platform.model.StrField
import amf.core.client.platform.model.domain.{DomainElement, NamedDomainElement, ObjectNode}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
abstract class GooglePubSubChannelBinding(override private[amf] val _internal: InternalGooglePubSubChannelBinding)
    extends ChannelBinding
    with BindingVersion {
  override def bindingVersion: StrField = _internal.bindingVersion

  def labels: ObjectNode                                     = _internal.labels
  def messageRetentionDuration: StrField                     = _internal.messageRetentionDuration
  def messageStoragePolicy: GooglePubSubMessageStoragePolicy = _internal.messageStoragePolicy
  def schemaSettings: GooglePubSubSchemaSettings             = _internal.schemaSettings

  def withLabels(labels: ObjectNode): this.type = {
    _internal.withLabels(labels)
    this
  }
  def withMessageRetentionDuration(messageRetentionDuration: String): this.type = {
    _internal.withMessageRetentionDuration(messageRetentionDuration)
    this
  }
  def withMessageStoragePolicy(messageStoragePolicy: GooglePubSubMessageStoragePolicy): this.type = {
    _internal.withMessageStoragePolicy(messageStoragePolicy._internal)
    this
  }
  def withSchemaSettings(schemaSettings: GooglePubSubSchemaSettings): this.type = {
    _internal.withSchemaSettings(schemaSettings._internal)
    this
  }

  override def withBindingVersion(bindingVersion: String): this.type = {
    _internal.withBindingVersion(bindingVersion)
    this
  }
}

@JSExportAll
case class GooglePubSubChannelBinding010(override private[amf] val _internal: InternalGooglePubSubChannelBinding010)
    extends GooglePubSubChannelBinding(_internal) {
  @JSExportTopLevel("GooglePubSubChannelBinding010")
  def this() = this(InternalGooglePubSubChannelBinding010())

  def topic: StrField = _internal.topic

  def withTopic(topic: String): this.type = {
    _internal.withTopic(topic)
    this
  }

  override def linkCopy(): GooglePubSubChannelBinding010 = _internal.linkCopy()
}

@JSExportAll
case class GooglePubSubChannelBinding020(override private[amf] val _internal: InternalGooglePubSubChannelBinding020)
    extends GooglePubSubChannelBinding(_internal) {
  @JSExportTopLevel("GooglePubSubChannelBinding020")
  def this() = this(InternalGooglePubSubChannelBinding020())
  override def linkCopy(): GooglePubSubChannelBinding020 = _internal.linkCopy()
}

@JSExportAll
case class GooglePubSubMessageStoragePolicy(
    override private[amf] val _internal: InternalGooglePubSubMessageStoragePolicy
) extends DomainElement {
  @JSExportTopLevel("GooglePubSubMessageStoragePolicy")
  def this() = this(InternalGooglePubSubMessageStoragePolicy())
  def allowedPersistenceRegions: ClientList[StrField] = _internal.allowedPersistenceRegions.asClient
  def withAllowedPersistenceRegions(allowedPersistenceRegions: ClientList[String]): this.type = {
    _internal.withAllowedPersistenceRegions(allowedPersistenceRegions.asInternal)
    this
  }
}

@JSExportAll
case class GooglePubSubSchemaSettings(override private[amf] val _internal: InternalGooglePubSubSchemaSettings)
    extends DomainElement
    with NamedDomainElement {
  @JSExportTopLevel("GooglePubSubSchemaSettings")
  def this() = this(InternalGooglePubSubSchemaSettings())

  override def name: StrField = _internal.name

  override def withName(name: String): this.type = {
    _internal.withName(name)
    this
  }

  def encoding: StrField        = _internal.encoding
  def firstRevisionId: StrField = _internal.firstRevisionId
  def lastRevisionId: StrField  = _internal.lastRevisionId

  def withEncoding(encoding: String): this.type = {
    _internal.withEncoding(encoding)
    this
  }
  def withFirstRevisionId(firstRevisionId: String): this.type = {
    _internal.withFirstRevisionId(firstRevisionId)
    this
  }
  def withLastRevisionId(lastRevisionId: String): this.type = {
    _internal.withLastRevisionId(lastRevisionId)
    this
  }
}
