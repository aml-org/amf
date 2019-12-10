package amf.plugins.domain.webapi.models.bindings.kafka
import amf.core.metamodel.{Field, Obj}
import amf.core.model.StrField
import amf.core.model.domain.{Linkable, DomainElement}
import amf.core.parser.{Annotations, Fields}
import amf.plugins.domain.webapi.metamodel.bindings.KafkaOperationBindingModel
import amf.plugins.domain.webapi.metamodel.bindings.KafkaOperationBindingModel._
import amf.plugins.domain.webapi.models.bindings.{OperationBinding, BindingVersion}

class KafkaOperationBinding(override val fields: Fields, override val annotations: Annotations)
    extends OperationBinding
    with BindingVersion {

  def groupId: StrField  = fields.field(GroupId)
  def clientId: StrField = fields.field(ClientId)

  override def meta: Obj = KafkaOperationBindingModel

  override def componentId: String =
    s"${groupId.option().getOrElse("default-group")}/${clientId.option().getOrElse("default-client")}}"

  override protected def bindingVersionField: Field = BindingVersion

  def withGroupId(groupId: String): this.type   = set(GroupId, groupId)
  def withClientId(clientId: String): this.type = set(ClientId, clientId)
  override def linkCopy(): KafkaOperationBinding = KafkaOperationBinding().withId(id)

  /** apply method for create a new instance with fields and annotations. Aux method for copy */
  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement = KafkaOperationBinding.apply
}

object KafkaOperationBinding {

  def apply(): KafkaOperationBinding = apply(Annotations())

  def apply(annotations: Annotations): KafkaOperationBinding = apply(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): KafkaOperationBinding = new KafkaOperationBinding(fields, annotations)
}
