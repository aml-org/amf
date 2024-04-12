package amf.apicontract.client.platform.model.domain.bindings.googlepubsub

import amf.apicontract.client.platform.model.domain.bindings.{BindingVersion, MessageBinding}
import amf.apicontract.client.scala.model.domain.bindings.googlepubsub.{GooglePubSubMessageBinding => InternalGooglePubSubMessageBinding, GooglePubSubSchemaDefinition => InternalGooglePubSubSchemaDefinition}
import amf.core.client.platform.model.StrField
import amf.core.client.platform.model.domain.{DomainElement, ObjectNode}
import amf.apicontract.internal.convert.ApiClientConverters._

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}


@JSExportAll
case class GooglePubSubMessageBinding(override private[amf] val _internal: InternalGooglePubSubMessageBinding)
  extends MessageBinding
  with BindingVersion {
  @JSExportTopLevel("GooglePubSubMessageBinding")
  def this() = this(InternalGooglePubSubMessageBinding())

  override protected def bindingVersion: StrField = _internal.bindingVersion
  def attributes: ObjectNode = _internal.attributes
  def orderingKey: StrField = _internal.orderingKey
  def schema: GooglePubSubSchemaDefinition = new GooglePubSubSchemaDefinition(_internal.schema)

  def withAttributes(attributes: ObjectNode): this.type = {
    _internal.withAttributes(attributes)
    this
  }
  def withOrderingKey(orderingKey: String): this.type = {
    _internal.withOrderingKey(orderingKey)
    this
  }
  def withSchema(schema: GooglePubSubSchemaDefinition): this.type = {
    _internal.withSchema(schema)
    this
  }
  override def withBindingVersion(bindingVersion: String): GooglePubSubMessageBinding.this.type = {
    _internal.withBindingVersion(bindingVersion)
    this
  }

  override def linkCopy(): GooglePubSubMessageBinding = new GooglePubSubMessageBinding(_internal.linkCopy())

}

@JSExportAll
case class GooglePubSubSchemaDefinition(override private[amf] val _internal: InternalGooglePubSubSchemaDefinition) extends DomainElement {
  @JSExportTopLevel("GooglePubSubSchemaDefinition")
  def this() = this(InternalGooglePubSubSchemaDefinition())

  def name: StrField = _internal.name
  def fieldType: StrField = _internal.fieldType

  def withName(name: String): this.type = {
    _internal.withName(name)
    this
  }
  def withFieldType(fieldType: String): this.type = {
    _internal.withFieldType(fieldType)
    this
  }

}
