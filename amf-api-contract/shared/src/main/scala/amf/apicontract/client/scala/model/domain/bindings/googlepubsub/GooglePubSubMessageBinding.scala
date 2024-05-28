package amf.apicontract.client.scala.model.domain.bindings.googlepubsub

import amf.apicontract.client.scala.model.domain.bindings.{BindingVersion, MessageBinding}
import amf.apicontract.internal.metamodel.domain.bindings.BindingVersion.BindingVersion
import amf.apicontract.internal.metamodel.domain.bindings.GooglePubSubMessageBindingModel._
import amf.apicontract.internal.metamodel.domain.bindings.{
  GooglePubSubMessageBinding010Model,
  GooglePubSubMessageBinding020Model,
  GooglePubSubMessageBindingModel,
  GooglePubSubSchemaDefinition010Model,
  GooglePubSubSchemaDefinition020Model,
  GooglePubSubSchemaDefinitionModel
}
import amf.apicontract.internal.spec.async.parser.bindings.Bindings.GooglePubSub
import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.domain.{DomainElement, Linkable, ObjectNode}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.shapes.client.scala.model.domain.Key

abstract class GooglePubSubMessageBinding(override val fields: Fields, override val annotations: Annotations)
    extends MessageBinding
    with BindingVersion
    with Key {
  override protected def bindingVersionField: Field = BindingVersion
  override def key: StrField                        = fields.field(GooglePubSubMessageBindingModel.key)
  override def componentId: String                  = s"/$GooglePubSub-message"

  def attributes: ObjectNode = fields.field(Attributes)
  def orderingKey: StrField  = fields.field(OrderingKey)

  def withAttributes(attributes: ObjectNode): this.type = set(Attributes, attributes)
  def withOrderingKey(orderingKey: String): this.type   = set(OrderingKey, orderingKey)
}

class GooglePubSubMessageBinding010(override val fields: Fields, override val annotations: Annotations)
    extends GooglePubSubMessageBinding(fields, annotations) {
  override def meta: GooglePubSubMessageBinding010Model.type = GooglePubSubMessageBinding010Model
  override def componentId: String                           = s"/$GooglePubSub-message-010"

  def schema: GooglePubSubSchemaDefinition010                        = fields.field(Schema)
  def withSchema(schema: GooglePubSubSchemaDefinition010): this.type = set(Schema, schema)

  override def linkCopy(): GooglePubSubMessageBinding010 = GooglePubSubMessageBinding010().withId(id)

  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement =
    GooglePubSubMessageBinding010.apply
}

object GooglePubSubMessageBinding010 {
  def apply(): GooglePubSubMessageBinding010                         = apply(Annotations())
  def apply(annotations: Annotations): GooglePubSubMessageBinding010 = apply(Fields(), annotations)
  def apply(fields: Fields, annotations: Annotations): GooglePubSubMessageBinding010 =
    new GooglePubSubMessageBinding010(fields, annotations)
}

class GooglePubSubMessageBinding020(override val fields: Fields, override val annotations: Annotations)
    extends GooglePubSubMessageBinding(fields, annotations) {
  override def meta: GooglePubSubMessageBinding020Model.type = GooglePubSubMessageBinding020Model
  override def componentId: String                           = s"/$GooglePubSub-message-020"

  def schema: GooglePubSubSchemaDefinition020                        = fields.field(Schema)
  def withSchema(schema: GooglePubSubSchemaDefinition020): this.type = set(Schema, schema)

  override def linkCopy(): GooglePubSubMessageBinding020 = GooglePubSubMessageBinding020().withId(id)

  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement =
    GooglePubSubMessageBinding020.apply
}

object GooglePubSubMessageBinding020 {
  def apply(): GooglePubSubMessageBinding020                         = apply(Annotations())
  def apply(annotations: Annotations): GooglePubSubMessageBinding020 = apply(Fields(), annotations)
  def apply(fields: Fields, annotations: Annotations): GooglePubSubMessageBinding020 =
    new GooglePubSubMessageBinding020(fields, annotations)
}

abstract class GooglePubSubSchemaDefinition(val fields: Fields, val annotations: Annotations) extends DomainElement {
  def componentId: String = s"/$GooglePubSub-schemaDefinition"

  def name: StrField                    = fields.field(GooglePubSubSchemaDefinitionModel.Name)
  def withName(name: String): this.type = set(GooglePubSubSchemaDefinitionModel.Name, name)
}

class GooglePubSubSchemaDefinition010(override val fields: Fields, override val annotations: Annotations)
    extends GooglePubSubSchemaDefinition(fields, annotations) {
  def meta: GooglePubSubSchemaDefinition010Model.type = GooglePubSubSchemaDefinition010Model
  override def componentId: String                    = s"/$GooglePubSub-schemaDefinition-010"

  def fieldType: StrField                         = fields.field(GooglePubSubSchemaDefinition010Model.FieldType)
  def withFieldType(fieldType: String): this.type = set(GooglePubSubSchemaDefinition010Model.FieldType, fieldType)
}

object GooglePubSubSchemaDefinition010 {
  def apply(): GooglePubSubSchemaDefinition010                         = apply(Annotations())
  def apply(annotations: Annotations): GooglePubSubSchemaDefinition010 = apply(Fields(), annotations)
  def apply(fields: Fields, annotations: Annotations): GooglePubSubSchemaDefinition010 =
    new GooglePubSubSchemaDefinition010(fields, annotations)
}

class GooglePubSubSchemaDefinition020(override val fields: Fields, override val annotations: Annotations)
    extends GooglePubSubSchemaDefinition(fields, annotations) {
  def meta: GooglePubSubSchemaDefinition020Model.type = GooglePubSubSchemaDefinition020Model
  override def componentId: String                    = s"/$GooglePubSub-schemaDefinition-020"
}

object GooglePubSubSchemaDefinition020 {
  def apply(): GooglePubSubSchemaDefinition020                         = apply(Annotations())
  def apply(annotations: Annotations): GooglePubSubSchemaDefinition020 = apply(Fields(), annotations)
  def apply(fields: Fields, annotations: Annotations): GooglePubSubSchemaDefinition020 =
    new GooglePubSubSchemaDefinition020(fields, annotations)
}
