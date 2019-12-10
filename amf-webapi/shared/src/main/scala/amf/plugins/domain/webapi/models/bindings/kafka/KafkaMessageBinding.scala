package amf.plugins.domain.webapi.models.bindings.kafka
import amf.core.metamodel.{Field, Obj}
import amf.core.model.StrField
import amf.core.model.domain.{Linkable, DomainElement}
import amf.core.parser.{Annotations, Fields}
import amf.plugins.domain.webapi.metamodel.bindings.KafkaMessageBindingModel
import amf.plugins.domain.webapi.metamodel.bindings.KafkaMessageBindingModel._
import amf.plugins.domain.webapi.models.bindings.{BindingVersion, MessageBinding}

class KafkaMessageBinding(override val fields: Fields, override val annotations: Annotations)
    extends MessageBinding
    with BindingVersion {

  override protected def bindingVersionField: Field = BindingVersion
  override def meta: Obj                            = KafkaMessageBindingModel

  def key: StrField                   = fields.field(Key)
  def withKey(key: String): this.type = set(Key, key)

  override def componentId: String = key.option().getOrElse("KafkaMessageBinding")

  override def linkCopy(): KafkaMessageBinding = KafkaMessageBinding()

  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement = KafkaMessageBinding.apply
}

object KafkaMessageBinding {

  def apply(): KafkaMessageBinding = apply(Annotations())

  def apply(annotations: Annotations): KafkaMessageBinding = apply(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): KafkaMessageBinding = new KafkaMessageBinding(fields, annotations)
}
