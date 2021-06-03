package amf.plugins.domain.apicontract.models.bindings.kafka
import amf.core.metamodel.Field
import amf.core.model.StrField
import amf.core.model.domain.{DomainElement, Linkable, Shape}
import amf.core.parser.{Annotations, Fields}
import amf.plugins.domain.apicontract.metamodel.bindings.KafkaOperationBindingModel
import amf.plugins.domain.apicontract.metamodel.bindings.KafkaOperationBindingModel._
import amf.plugins.domain.apicontract.models.Key
import amf.plugins.domain.apicontract.models.bindings.{BindingVersion, OperationBinding}

class KafkaOperationBinding(override val fields: Fields, override val annotations: Annotations)
    extends OperationBinding
    with BindingVersion
    with Key {

  def groupId: Shape  = fields.field(GroupId)
  def clientId: Shape = fields.field(ClientId)

  override def meta: KafkaOperationBindingModel.type = KafkaOperationBindingModel

  override def componentId: String = "/kafka-operation"

  override val key: StrField = fields.field(KafkaOperationBindingModel.key)

  override protected def bindingVersionField: Field = BindingVersion

  def withGroupId(groupId: Shape): this.type     = set(GroupId, groupId)
  def withClientId(clientId: Shape): this.type   = set(ClientId, clientId)
  override def linkCopy(): KafkaOperationBinding = KafkaOperationBinding().withId(id)

  /** apply method for create a new instance with fields and annotations. Aux method for copy */
  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement =
    KafkaOperationBinding.apply
}

object KafkaOperationBinding {

  def apply(): KafkaOperationBinding = apply(Annotations())

  def apply(annotations: Annotations): KafkaOperationBinding = apply(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): KafkaOperationBinding =
    new KafkaOperationBinding(fields, annotations)
}
