package amf.apicontract.client.scala.model.domain.bindings.kafka

import amf.apicontract.client.scala.model.domain.bindings.{BindingVersion, ServerBinding}
import amf.apicontract.internal.metamodel.domain.bindings.KafkaServerBindingModel
import amf.apicontract.internal.metamodel.domain.bindings.KafkaServerBindingModel._
import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.domain.{DomainElement, Linkable}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.shapes.client.scala.model.domain.Key

class KafkaServerBinding(override val fields: Fields, override val annotations: Annotations)
    extends ServerBinding
    with BindingVersion
    with Key {

  def schemaRegistryUrl: StrField    = fields.field(SchemaRegistryUrl)
  def schemaRegistryVendor: StrField = fields.field(SchemaRegistryVendor)

  override def meta: KafkaServerBindingModel.type = KafkaServerBindingModel

  override def componentId: String = "/kafka-server"

  override val key: StrField = fields.field(KafkaServerBindingModel.key)

  override protected def bindingVersionField: Field = BindingVersion

  def withSchemaRegistryUrl(schemaRegistryUrl: String): this.type = set(SchemaRegistryUrl, schemaRegistryUrl)
  def withSchemaRegistryVendor(schemaRegistryVendor: String): this.type =
    set(SchemaRegistryVendor, schemaRegistryVendor)

  override def linkCopy(): KafkaServerBinding = KafkaServerBinding().withId(id)

  /** apply method for create a new instance with fields and annotations. Aux method for copy */
  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement =
    KafkaServerBinding.apply
}

object KafkaServerBinding {

  def apply(): KafkaServerBinding = apply(Annotations())

  def apply(annotations: Annotations): KafkaServerBinding = apply(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): KafkaServerBinding =
    new KafkaServerBinding(fields, annotations)
}
