package amf.plugins.domain.apicontract.models.bindings.mqtt
import amf.core.metamodel.Field
import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.domain.{DomainElement, Linkable}
import amf.core.parser.{Annotations, Fields}
import amf.plugins.domain.apicontract.metamodel.bindings.MqttMessageBindingModel
import amf.plugins.domain.apicontract.metamodel.bindings.MqttMessageBindingModel._
import amf.plugins.domain.apicontract.models.Key
import amf.plugins.domain.apicontract.models.bindings.{BindingVersion, MessageBinding}

class MqttMessageBinding(override val fields: Fields, override val annotations: Annotations)
    extends MessageBinding
    with BindingVersion
    with Key {
  override protected def bindingVersionField: Field = BindingVersion
  override def meta: MqttMessageBindingModel.type   = MqttMessageBindingModel

  override val key: StrField = fields.field(MqttMessageBindingModel.key)

  override def componentId: String            = "/mqtt-message"
  override def linkCopy(): MqttMessageBinding = MqttMessageBinding().withId(id)

  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement =
    MqttMessageBinding.apply
}

object MqttMessageBinding {

  def apply(): MqttMessageBinding = apply(Annotations())

  def apply(annotations: Annotations): MqttMessageBinding = apply(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): MqttMessageBinding = new MqttMessageBinding(fields, annotations)
}
