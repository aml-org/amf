package amf.apicontract.client.scala.model.domain.bindings.kafka
import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.domain.{DomainElement, Linkable, Shape}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.apicontract.internal.metamodel.domain.bindings.KafkaMessageBindingModel
import amf.apicontract.internal.metamodel.domain.bindings.KafkaMessageBindingModel._
import amf.apicontract.client.scala.model.domain.bindings.{BindingVersion, MessageBinding}
import amf.shapes.client.scala.model.domain.Key

class KafkaMessageBinding(override val fields: Fields, override val annotations: Annotations)
    extends MessageBinding
    with BindingVersion
    with Key {

  override protected def bindingVersionField: Field = BindingVersion
  override def meta: KafkaMessageBindingModel.type  = KafkaMessageBindingModel

  def messageKey: Shape              = fields.field(MessageKey)
  def withKey(key: Shape): this.type = set(MessageKey, key)

  override def key: StrField = fields.field(KafkaMessageBindingModel.key)

  override def componentId: String = "/kafka-message"

  override def linkCopy(): KafkaMessageBinding = KafkaMessageBinding()

  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement =
    KafkaMessageBinding.apply
}

object KafkaMessageBinding {

  def apply(): KafkaMessageBinding = apply(Annotations())

  def apply(annotations: Annotations): KafkaMessageBinding = apply(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): KafkaMessageBinding =
    new KafkaMessageBinding(fields, annotations)
}
