package amf.plugins.domain.webapi.models.bindings.mqtt
import amf.core.metamodel.{Field, Obj}
import amf.core.model.domain.{DomainElement, Linkable}
import amf.core.model.{BoolField, IntField, StrField}
import amf.core.parser.{Annotations, Fields}
import amf.plugins.domain.webapi.metamodel.bindings.MqttServerBindingModel._
import amf.plugins.domain.webapi.metamodel.bindings.MqttServerLastWillModel._
import amf.plugins.domain.webapi.metamodel.bindings.{MqttServerBindingModel, MqttServerLastWillModel}
import amf.plugins.domain.webapi.models.Key
import amf.plugins.domain.webapi.models.bindings.{BindingVersion, ServerBinding}

class MqttServerBinding(override val fields: Fields, override val annotations: Annotations)
    extends ServerBinding
    with BindingVersion
    with Key {
  override protected def bindingVersionField: Field = BindingVersion
  override def meta: Obj                            = MqttServerBindingModel

  def clientId: StrField           = fields.field(ClientId)
  def cleanSession: BoolField      = fields.field(CleanSession)
  def lastWill: MqttServerLastWill = fields.field(LastWill)
  def keepAlive: IntField          = fields.field(KeepAlive)

  def withClientId(clientId: String): this.type             = set(ClientId, clientId)
  def withCleanSession(cleanSession: Boolean): this.type    = set(CleanSession, cleanSession)
  def withLastWill(lastWill: MqttServerLastWill): this.type = set(LastWill, lastWill)
  def withKeepAlive(keepAlive: Int): this.type              = set(KeepAlive, keepAlive)

  override def componentId: String           = "/mqtt-server"
  override def linkCopy(): MqttServerBinding = MqttServerBinding().withId(id)

  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement =
    MqttServerBinding.apply

  override def key: StrField = fields.field(MqttServerBindingModel.key)
}

object MqttServerBinding {

  def apply(): MqttServerBinding = apply(Annotations())

  def apply(annotations: Annotations): MqttServerBinding = apply(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): MqttServerBinding = new MqttServerBinding(fields, annotations)
}

class MqttServerLastWill(override val fields: Fields, override val annotations: Annotations) extends DomainElement {
  override def meta: Obj = MqttServerLastWillModel

  def topic: StrField   = fields.field(Topic)
  def qos: IntField     = fields.field(Qos)
  def retain: BoolField = fields.field(Retain)

  def withTopic(topic: String): this.type    = set(Topic, topic)
  def withQos(qos: Int): this.type           = set(Qos, qos)
  def withRetain(retain: Boolean): this.type = set(Retain, retain)

  override def componentId: String = "/mqtt-last-will"
}

object MqttServerLastWill {

  def apply(): MqttServerLastWill = apply(Annotations())

  def apply(annotations: Annotations): MqttServerLastWill = apply(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): MqttServerLastWill = new MqttServerLastWill(fields, annotations)
}
