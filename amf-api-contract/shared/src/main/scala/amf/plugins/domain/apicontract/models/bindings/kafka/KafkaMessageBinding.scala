package amf.plugins.domain.apicontract.models.bindings.kafka
import amf.core.metamodel.Field
import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.domain.{DomainElement, Linkable, Shape}
import amf.core.parser.{Annotations, Fields}
import amf.plugins.domain.apicontract.metamodel.bindings.KafkaMessageBindingModel
import amf.plugins.domain.apicontract.metamodel.bindings.KafkaMessageBindingModel._
import amf.plugins.domain.apicontract.models.Key
import amf.plugins.domain.apicontract.models.bindings.{BindingVersion, MessageBinding}

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
