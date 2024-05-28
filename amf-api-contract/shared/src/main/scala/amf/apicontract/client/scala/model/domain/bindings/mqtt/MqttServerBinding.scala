package amf.apicontract.client.scala.model.domain.bindings.mqtt
import amf.core.client.scala.model.domain.{DomainElement, Linkable, Shape}
import amf.core.client.scala.model.{BoolField, IntField, StrField}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.apicontract.internal.metamodel.domain.bindings.MqttServerBindingModel._
import amf.apicontract.internal.metamodel.domain.bindings.MqttServerLastWillModel._
import amf.apicontract.internal.metamodel.domain.bindings.{
  MqttServerBinding010Model,
  MqttServerBinding020Model,
  MqttServerBindingModel,
  MqttServerLastWillModel
}
import amf.apicontract.client.scala.model.domain.bindings.{BindingVersion, ServerBinding}
import amf.apicontract.internal.metamodel.domain.bindings.MqttServerBinding020Model.{
  MaximumPacketSize,
  MaximumPacketSizeSchema,
  SessionExpiryInterval,
  SessionExpiryIntervalSchema
}
import amf.shapes.client.scala.model.domain.Key

abstract class MqttServerBinding(override val fields: Fields, override val annotations: Annotations)
    extends ServerBinding
    with BindingVersion
    with Key {
  override protected def bindingVersionField: Field = BindingVersion
  override def key: StrField                        = fields.field(MqttServerBindingModel.key)
  override def componentId: String                  = "/mqtt-server"

  def clientId: StrField           = fields.field(ClientId)
  def cleanSession: BoolField      = fields.field(CleanSession)
  def lastWill: MqttServerLastWill = fields.field(LastWill)
  def keepAlive: IntField          = fields.field(KeepAlive)

  def withClientId(clientId: String): this.type             = set(ClientId, clientId)
  def withCleanSession(cleanSession: Boolean): this.type    = set(CleanSession, cleanSession)
  def withLastWill(lastWill: MqttServerLastWill): this.type = set(LastWill, lastWill)
  def withKeepAlive(keepAlive: Int): this.type              = set(KeepAlive, keepAlive)
}

class MqttServerBinding010(override val fields: Fields, override val annotations: Annotations)
    extends MqttServerBinding(fields, annotations) {
  override def meta: MqttServerBinding010Model.type = MqttServerBinding010Model
  override def componentId: String                  = "/mqtt-server-010"
  override def linkCopy(): MqttServerBinding010     = MqttServerBinding010().withId(id)
  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement =
    MqttServerBinding010.apply
}

object MqttServerBinding010 {
  def apply(): MqttServerBinding010                         = apply(Annotations())
  def apply(annotations: Annotations): MqttServerBinding010 = apply(Fields(), annotations)
  def apply(fields: Fields, annotations: Annotations): MqttServerBinding010 =
    new MqttServerBinding010(fields, annotations)
}

class MqttServerBinding020(override val fields: Fields, override val annotations: Annotations)
    extends MqttServerBinding(fields, annotations) {
  override def meta: MqttServerBinding020Model.type = MqttServerBinding020Model
  override def componentId: String                  = "/mqtt-server-020"

  def sessionExpiryInterval: IntField    = fields.field(SessionExpiryInterval)
  def sessionExpiryIntervalSchema: Shape = fields.field(SessionExpiryIntervalSchema)

  def maximumPacketSize: IntField    = fields.field(MaximumPacketSize)
  def maximumPacketSizeSchema: Shape = fields.field(MaximumPacketSizeSchema)

  def withSessionExpiryInterval(sessionExpiryInterval: Int): this.type =
    set(SessionExpiryInterval, sessionExpiryInterval)
  def withSessionExpiryIntervalSchema(sessionExpiryIntervalSchema: Shape): this.type =
    set(SessionExpiryIntervalSchema, sessionExpiryIntervalSchema)

  def withMaximumPacketSize(maximumPacketSize: Int): this.type =
    set(MaximumPacketSize, maximumPacketSize)
  def withMaximumPacketSizeSchema(maximumPacketSizeSchema: Shape): this.type =
    set(MaximumPacketSizeSchema, maximumPacketSizeSchema)

  override def linkCopy(): MqttServerBinding020 = MqttServerBinding020().withId(id)
  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement =
    MqttServerBinding020.apply
}

object MqttServerBinding020 {
  def apply(): MqttServerBinding020                         = apply(Annotations())
  def apply(annotations: Annotations): MqttServerBinding020 = apply(Fields(), annotations)
  def apply(fields: Fields, annotations: Annotations): MqttServerBinding020 =
    new MqttServerBinding020(fields, annotations)
}

class MqttServerLastWill(override val fields: Fields, override val annotations: Annotations) extends DomainElement {
  override def meta: MqttServerLastWillModel.type = MqttServerLastWillModel
  override def componentId: String                = "/mqtt-last-will"

  def topic: StrField   = fields.field(Topic)
  def qos: IntField     = fields.field(Qos)
  def retain: BoolField = fields.field(Retain)
  def message: StrField = fields.field(Message)

  def withTopic(topic: String): this.type     = set(Topic, topic)
  def withQos(qos: Int): this.type            = set(Qos, qos)
  def withRetain(retain: Boolean): this.type  = set(Retain, retain)
  def withMessage(message: String): this.type = set(Message, message)
}

object MqttServerLastWill {
  def apply(): MqttServerLastWill                                         = apply(Annotations())
  def apply(annotations: Annotations): MqttServerLastWill                 = apply(Fields(), annotations)
  def apply(fields: Fields, annotations: Annotations): MqttServerLastWill = new MqttServerLastWill(fields, annotations)
}
