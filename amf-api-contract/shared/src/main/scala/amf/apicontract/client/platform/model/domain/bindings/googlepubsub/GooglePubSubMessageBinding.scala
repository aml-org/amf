package amf.apicontract.client.platform.model.domain.bindings.googlepubsub

import amf.apicontract.client.platform.model.domain.bindings.{BindingVersion, MessageBinding}
import amf.apicontract.client.scala.model.domain.bindings.googlepubsub.{
  GooglePubSubMessageBinding => InternalGooglePubSubMessageBinding,
  GooglePubSubMessageBinding010 => InternalGooglePubSubMessageBinding010,
  GooglePubSubMessageBinding020 => InternalGooglePubSubMessageBinding020,
  GooglePubSubSchemaDefinition => InternalGooglePubSubSchemaDefinition,
  GooglePubSubSchemaDefinition010 => InternalGooglePubSubSchemaDefinition010,
  GooglePubSubSchemaDefinition020 => InternalGooglePubSubSchemaDefinition020
}
import amf.core.client.platform.model.StrField
import amf.core.client.platform.model.domain.{DomainElement, ObjectNode}
import amf.apicontract.internal.convert.ApiClientConverters._

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
abstract class GooglePubSubMessageBinding(override private[amf] val _internal: InternalGooglePubSubMessageBinding)
    extends MessageBinding
    with BindingVersion {
  override protected def bindingVersion: StrField = _internal.bindingVersion
  override def withBindingVersion(bindingVersion: String): GooglePubSubMessageBinding.this.type = {
    _internal.withBindingVersion(bindingVersion)
    this
  }

  def attributes: ObjectNode = _internal.attributes
  def orderingKey: StrField  = _internal.orderingKey
  def schema: GooglePubSubSchemaDefinition

  def withAttributes(attributes: ObjectNode): this.type = {
    _internal.withAttributes(attributes)
    this
  }
  def withOrderingKey(orderingKey: String): this.type = {
    _internal.withOrderingKey(orderingKey)
    this
  }
}

@JSExportAll
case class GooglePubSubMessageBinding010(override private[amf] val _internal: InternalGooglePubSubMessageBinding010)
    extends GooglePubSubMessageBinding(_internal) {
  @JSExportTopLevel("GooglePubSubMessageBinding010")
  def this() = this(InternalGooglePubSubMessageBinding010())

  def schema: GooglePubSubSchemaDefinition010 = GooglePubSubSchemaDefinition010(_internal.schema)

  def withSchema(schema: GooglePubSubSchemaDefinition010): this.type = {
    _internal.withSchema(schema)
    this
  }

  override def linkCopy(): GooglePubSubMessageBinding010 = GooglePubSubMessageBinding010(_internal.linkCopy())
}

@JSExportAll
case class GooglePubSubMessageBinding020(override private[amf] val _internal: InternalGooglePubSubMessageBinding020)
    extends GooglePubSubMessageBinding(_internal) {
  @JSExportTopLevel("GooglePubSubMessageBinding020")
  def this() = this(InternalGooglePubSubMessageBinding020())

  def schema: GooglePubSubSchemaDefinition020 = GooglePubSubSchemaDefinition020(_internal.schema)

  def withSchema(schema: GooglePubSubSchemaDefinition020): this.type = {
    _internal.withSchema(schema)
    this
  }

  override def linkCopy(): GooglePubSubMessageBinding020 = GooglePubSubMessageBinding020(_internal.linkCopy())
}

@JSExportAll
abstract class GooglePubSubSchemaDefinition(override private[amf] val _internal: InternalGooglePubSubSchemaDefinition)
    extends DomainElement {
  def name: StrField = _internal.name
  def withName(name: String): this.type = {
    _internal.withName(name)
    this
  }
}

@JSExportAll
case class GooglePubSubSchemaDefinition010(override private[amf] val _internal: InternalGooglePubSubSchemaDefinition010)
    extends GooglePubSubSchemaDefinition(_internal) {
  @JSExportTopLevel("GooglePubSubSchemaDefinition010")
  def this() = this(InternalGooglePubSubSchemaDefinition010())
  def fieldType: StrField = _internal.fieldType
  def withFieldType(fieldType: String): this.type = {
    _internal.withFieldType(fieldType)
    this
  }
}

@JSExportAll
case class GooglePubSubSchemaDefinition020(override private[amf] val _internal: InternalGooglePubSubSchemaDefinition020)
    extends GooglePubSubSchemaDefinition(_internal) {
  @JSExportTopLevel("GooglePubSubSchemaDefinition020")
  def this() = this(InternalGooglePubSubSchemaDefinition020())
}
