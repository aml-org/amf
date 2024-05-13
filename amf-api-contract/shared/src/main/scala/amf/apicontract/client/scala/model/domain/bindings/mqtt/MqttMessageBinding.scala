package amf.apicontract.client.scala.model.domain.bindings.mqtt
import amf.core.client.scala.model.{IntField, StrField}
import amf.core.client.scala.model.domain.{DomainElement, Linkable, Shape}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.apicontract.internal.metamodel.domain.bindings.{
  MqttMessageBinding010Model,
  MqttMessageBinding020Model,
  MqttMessageBindingModel
}
import amf.apicontract.internal.metamodel.domain.bindings.MqttMessageBindingModel._
import amf.apicontract.client.scala.model.domain.bindings.{BindingVersion, MessageBinding}
import amf.apicontract.internal.metamodel.domain.bindings.MqttMessageBinding020Model.{
  CorrelationData,
  PayloadFormatIndicator,
  ContentType,
  ResponseTopic
}
import amf.shapes.client.scala.model.domain.Key

abstract class MqttMessageBinding(override val fields: Fields, override val annotations: Annotations)
    extends MessageBinding
    with BindingVersion
    with Key {
  override protected def bindingVersionField: Field = BindingVersion
  override val key: StrField                        = fields.field(MqttMessageBindingModel.key)
  override def componentId: String                  = "/mqtt-message"
}

class MqttMessageBinding010(override val fields: Fields, override val annotations: Annotations)
    extends MqttMessageBinding(fields, annotations) {
  override def componentId: String                   = "/mqtt-message-010"
  override def meta: MqttMessageBinding010Model.type = MqttMessageBinding010Model
  override def linkCopy(): MqttMessageBinding010     = MqttMessageBinding010().withId(id)
  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement =
    MqttMessageBinding010.apply
}

object MqttMessageBinding010 {
  def apply(): MqttMessageBinding010                         = apply(Annotations())
  def apply(annotations: Annotations): MqttMessageBinding010 = apply(Fields(), annotations)
  def apply(fields: Fields, annotations: Annotations): MqttMessageBinding010 =
    new MqttMessageBinding010(fields, annotations)
}

class MqttMessageBinding020(override val fields: Fields, override val annotations: Annotations)
    extends MqttMessageBinding(fields, annotations) {
  override def componentId: String                   = "/mqtt-message-020"
  override def meta: MqttMessageBinding020Model.type = MqttMessageBinding020Model

  def payloadFormatIndicator: IntField = fields.field(PayloadFormatIndicator)
  def correlationData: Shape           = fields.field(CorrelationData)
  def contentType: StrField            = fields.field(ContentType)
  def responseTopic: StrField          = fields.field(ResponseTopic)

  def withPayloadFormatIndicator(payloadFormatIndicator: Int): this.type =
    set(PayloadFormatIndicator, payloadFormatIndicator)
  def withCorrelationData(correlationData: Shape): this.type = set(CorrelationData, correlationData)
  def withContentType(contentType: String): this.type        = set(ContentType, contentType)
  def withResponseTopic(responseTopic: String): this.type    = set(ResponseTopic, responseTopic)

  override def linkCopy(): MqttMessageBinding020 = MqttMessageBinding020().withId(id)
  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement =
    MqttMessageBinding020.apply
}

object MqttMessageBinding020 {
  def apply(): MqttMessageBinding020                         = apply(Annotations())
  def apply(annotations: Annotations): MqttMessageBinding020 = apply(Fields(), annotations)
  def apply(fields: Fields, annotations: Annotations): MqttMessageBinding020 =
    new MqttMessageBinding020(fields, annotations)
}
