package amf.apicontract.client.scala.model.domain.bindings.mqtt
import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.domain.{DomainElement, Linkable}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.apicontract.internal.metamodel.domain.bindings.MqttMessageBindingModel
import amf.apicontract.internal.metamodel.domain.bindings.MqttMessageBindingModel._
import amf.apicontract.client.scala.model.domain.bindings.{BindingVersion, MessageBinding}
import amf.shapes.client.scala.model.domain.Key

class MqttMessageBinding(override val fields: Fields, override val annotations: Annotations)
    extends MessageBinding
    with BindingVersion
    with Key {
  override protected def bindingVersionField: Field = BindingVersion
  override def meta: MqttMessageBindingModel.type   = MqttMessageBindingModel

  override val key: StrField = fields.field(MqttMessageBindingModel.key)

  private[amf] override def componentId: String = "/mqtt-message"
  override def linkCopy(): MqttMessageBinding   = MqttMessageBinding().withId(id)

  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement =
    MqttMessageBinding.apply
}

object MqttMessageBinding {

  def apply(): MqttMessageBinding = apply(Annotations())

  def apply(annotations: Annotations): MqttMessageBinding = apply(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): MqttMessageBinding = new MqttMessageBinding(fields, annotations)
}
