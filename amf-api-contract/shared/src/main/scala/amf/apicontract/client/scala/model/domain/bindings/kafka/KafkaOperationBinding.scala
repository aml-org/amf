package amf.apicontract.client.scala.model.domain.bindings.kafka
import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.domain.{DomainElement, Linkable, Shape}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.apicontract.internal.metamodel.domain.bindings.KafkaOperationBindingModel
import amf.apicontract.internal.metamodel.domain.bindings.KafkaOperationBindingModel._
import amf.apicontract.client.scala.model.domain.bindings.{BindingVersion, OperationBinding}
import amf.apicontract.internal.transformation.stages.Key

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
