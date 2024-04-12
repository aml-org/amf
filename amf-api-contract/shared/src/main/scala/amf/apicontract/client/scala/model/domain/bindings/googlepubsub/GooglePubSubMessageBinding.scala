package amf.apicontract.client.scala.model.domain.bindings.googlepubsub

import amf.apicontract.client.scala.model.domain.bindings.{BindingVersion, MessageBinding}
import amf.apicontract.internal.metamodel.domain.bindings.BindingVersion.BindingVersion
import amf.apicontract.internal.metamodel.domain.bindings.GooglePubSubMessageBindingModel._
import amf.apicontract.internal.metamodel.domain.bindings.{
  GooglePubSubMessageBindingModel,
  GooglePubSubSchemaDefinitionModel
}
import amf.apicontract.internal.spec.async.parser.bindings.Bindings.GooglePubSub
import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.domain.{DomainElement, Linkable, ObjectNode}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.shapes.client.scala.model.domain.Key

class GooglePubSubMessageBinding(override val fields: Fields, override val annotations: Annotations)
    extends MessageBinding
    with BindingVersion
    with Key {

  override protected def bindingVersionField: Field               = BindingVersion
  override def meta: GooglePubSubMessageBindingModel.type         = GooglePubSubMessageBindingModel
  def attributes: ObjectNode                                      = fields.field(Attributes)
  def orderingKey: StrField                                       = fields.field(OrderingKey)
  def schema: GooglePubSubSchemaDefinition                        = fields.field(Schema)
  def withAttributes(attributes: ObjectNode): this.type           = set(Attributes, attributes)
  def withOrderingKey(orderingKey: String): this.type             = set(OrderingKey, orderingKey)
  def withSchema(schema: GooglePubSubSchemaDefinition): this.type = set(Schema, schema)

  override def key: StrField = fields.field(GooglePubSubMessageBindingModel.key)

  override def componentId: String = s"/$GooglePubSub-message"

  override def linkCopy(): GooglePubSubMessageBinding = GooglePubSubMessageBinding().withId(id)

  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement =
    GooglePubSubMessageBinding.apply

}

object GooglePubSubMessageBinding {
  def apply(): GooglePubSubMessageBinding                         = apply(Annotations())
  def apply(annotations: Annotations): GooglePubSubMessageBinding = apply(Fields(), annotations)
  def apply(fields: Fields, annotations: Annotations): GooglePubSubMessageBinding =
    new GooglePubSubMessageBinding(fields, annotations)

}

class GooglePubSubSchemaDefinition(val fields: Fields, val annotations: Annotations) extends DomainElement {
  def meta: GooglePubSubSchemaDefinitionModel.type = GooglePubSubSchemaDefinitionModel
  def name: StrField                               = fields.field(GooglePubSubSchemaDefinitionModel.Name)
  def fieldType: StrField                          = fields.field(GooglePubSubSchemaDefinitionModel.FieldType)
  def withName(name: String): this.type            = set(GooglePubSubSchemaDefinitionModel.Name, name)
  def withFieldType(fieldType: String): this.type  = set(GooglePubSubSchemaDefinitionModel.FieldType, fieldType)
  def componentId: String                          = s"/$GooglePubSub-schemaDefinition"

}

object GooglePubSubSchemaDefinition {
  def apply(): GooglePubSubSchemaDefinition                         = apply(Annotations())
  def apply(annotations: Annotations): GooglePubSubSchemaDefinition = apply(Fields(), annotations)
  def apply(fields: Fields, annotations: Annotations): GooglePubSubSchemaDefinition =
    new GooglePubSubSchemaDefinition(fields, annotations)

}
