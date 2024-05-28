package amf.apicontract.client.scala.model.domain.bindings.mqtt
import amf.core.client.scala.model.domain.{DomainElement, Linkable, Shape}
import amf.core.client.scala.model.{BoolField, IntField, StrField}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.apicontract.internal.metamodel.domain.bindings.{
  MqttOperationBinding010Model,
  MqttOperationBinding020Model,
  MqttOperationBindingModel
}
import amf.apicontract.internal.metamodel.domain.bindings.MqttOperationBindingModel._
import amf.apicontract.client.scala.model.domain.bindings.{BindingVersion, OperationBinding}
import amf.apicontract.internal.metamodel.domain.bindings.MqttOperationBinding020Model.{
  MessageExpiryInterval,
  MessageExpiryIntervalSchema
}
import amf.shapes.client.scala.model.domain.Key

abstract class MqttOperationBinding(override val fields: Fields, override val annotations: Annotations)
    extends OperationBinding
    with BindingVersion
    with Key {
  override protected def bindingVersionField: Field = BindingVersion
  override def componentId: String                  = "/mqtt-operation"
  override def key: StrField                        = fields.field(MqttOperationBindingModel.key)

  def qos: IntField     = fields.field(Qos)
  def retain: BoolField = fields.field(Retain)

  def withQos(qos: Int): this.type           = set(Qos, qos)
  def withRetain(retain: Boolean): this.type = set(Retain, retain)
}

class MqttOperationBinding010(override val fields: Fields, override val annotations: Annotations)
    extends MqttOperationBinding(fields, annotations) {
  override def componentId: String                     = "/mqtt-operation-010"
  override def meta: MqttOperationBinding010Model.type = MqttOperationBinding010Model

  override def linkCopy(): MqttOperationBinding010 = MqttOperationBinding010().withId(id)

  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement =
    MqttOperationBinding010.apply
}

object MqttOperationBinding010 {
  def apply(): MqttOperationBinding010                         = apply(Annotations())
  def apply(annotations: Annotations): MqttOperationBinding010 = apply(Fields(), annotations)
  def apply(fields: Fields, annotations: Annotations): MqttOperationBinding010 =
    new MqttOperationBinding010(fields, annotations)
}

class MqttOperationBinding020(override val fields: Fields, override val annotations: Annotations)
    extends MqttOperationBinding(fields, annotations) {
  override def componentId: String                     = "/mqtt-operation-020"
  override def meta: MqttOperationBinding020Model.type = MqttOperationBinding020Model

  def messageExpiryInterval: IntField    = fields.field(MessageExpiryInterval)
  def messageExpiryIntervalSchema: Shape = fields.field(MessageExpiryIntervalSchema)

  def withMessageExpiryInterval(messageExpiryInterval: Int): this.type =
    set(MessageExpiryInterval, messageExpiryInterval)
  def withMessageExpiryIntervalSchema(messageExpiryIntervalSchema: Shape): this.type =
    set(MessageExpiryIntervalSchema, messageExpiryIntervalSchema)

  override def linkCopy(): MqttOperationBinding020 = MqttOperationBinding020().withId(id)

  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement =
    MqttOperationBinding020.apply
}

object MqttOperationBinding020 {
  def apply(): MqttOperationBinding020                         = apply(Annotations())
  def apply(annotations: Annotations): MqttOperationBinding020 = apply(Fields(), annotations)
  def apply(fields: Fields, annotations: Annotations): MqttOperationBinding020 =
    new MqttOperationBinding020(fields, annotations)
}
