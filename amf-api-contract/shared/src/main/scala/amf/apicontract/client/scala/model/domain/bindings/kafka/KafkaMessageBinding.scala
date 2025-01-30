package amf.apicontract.client.scala.model.domain.bindings.kafka
import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.domain.{DomainElement, Linkable, Shape}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.apicontract.internal.metamodel.domain.bindings.{
  KafkaMessageBindingModel => BaseModel,
  KafkaMessageBinding010Model => Kafka010Model,
  KafkaMessageBinding030Model => Kafka030Model
}
import amf.apicontract.client.scala.model.domain.bindings.{BindingVersion, MessageBinding}
import amf.shapes.client.scala.model.domain.Key

abstract class KafkaMessageBinding(override val fields: Fields, override val annotations: Annotations)
    extends MessageBinding
    with BindingVersion
    with Key {
  override protected def bindingVersionField: Field = BaseModel.BindingVersion
  override def key: StrField                        = fields.field(BaseModel.key)
  override def componentId: String                  = "/kafka-message"

  def messageKey: Shape              = fields.field(BaseModel.MessageKey)
  def withKey(key: Shape): this.type = set(BaseModel.MessageKey, key)
}

class KafkaMessageBinding010(override val fields: Fields, override val annotations: Annotations)
    extends KafkaMessageBinding(fields, annotations) {
  override def meta: Kafka010Model.type        = Kafka010Model
  override def componentId: String             = "/kafka-message-010"
  override def linkCopy(): KafkaMessageBinding = KafkaMessageBinding010().withId(id)

  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement =
    KafkaMessageBinding010.apply
}

object KafkaMessageBinding010 {
  def apply(): KafkaMessageBinding010 = apply(Annotations())

  def apply(annotations: Annotations): KafkaMessageBinding010 = apply(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): KafkaMessageBinding010 =
    new KafkaMessageBinding010(fields, annotations)
}

class KafkaMessageBinding030(override val fields: Fields, override val annotations: Annotations)
    extends KafkaMessageBinding(fields, annotations) {
  override def meta: Kafka030Model.type        = Kafka030Model
  override def componentId: String             = "/kafka-message-030"
  override def linkCopy(): KafkaMessageBinding = KafkaMessageBinding030().withId(id)
  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement =
    KafkaMessageBinding030.apply

  def schemaIdLocation: StrField        = fields.field(Kafka030Model.SchemaIdLocation)
  def schemaIdPayloadEncoding: StrField = fields.field(Kafka030Model.SchemaIdPayloadEncoding)
  def schemaLookupStrategy: StrField    = fields.field(Kafka030Model.SchemaLookupStrategy)

  def withSchemaIdLocation(schemaIdLocation: String): this.type = set(Kafka030Model.SchemaIdLocation, schemaIdLocation)
  def withSchemaIdPayloadEncoding(schemaIdPayloadEncoding: String): this.type =
    set(Kafka030Model.SchemaIdPayloadEncoding, schemaIdPayloadEncoding)
  def withSchemaLookupStrategy(schemaLookupStrategy: String): this.type =
    set(Kafka030Model.SchemaLookupStrategy, schemaLookupStrategy)
}

object KafkaMessageBinding030 {

  def apply(): KafkaMessageBinding030 = apply(Annotations())

  def apply(annotations: Annotations): KafkaMessageBinding030 = apply(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): KafkaMessageBinding030 =
    new KafkaMessageBinding030(fields, annotations)
}
