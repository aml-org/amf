package amf.apicontract.client.scala.model.domain.bindings.amqp

import amf.core.client.scala.model.domain.{DomainElement, Linkable}
import amf.core.client.scala.model.{BoolField, IntField, StrField}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.apicontract.internal.metamodel.domain.bindings.{
  Amqp091OperationBinding010Model => Amqp010Model,
  Amqp091OperationBinding030Model => Amqp030Model,
  Amqp091OperationBindingModel => BaseModel
}
import amf.apicontract.internal.metamodel.domain.bindings.BindingVersion.BindingVersion
import amf.apicontract.client.scala.model.domain.bindings.{BindingVersion, OperationBinding}
import amf.shapes.client.scala.model.domain.Key

abstract class Amqp091OperationBinding(override val fields: Fields, override val annotations: Annotations)
    extends OperationBinding
    with BindingVersion
    with Key {
  override def key: StrField                        = fields.field(BaseModel.key)
  override protected def bindingVersionField: Field = BindingVersion

  def expiration: IntField   = fields.field(BaseModel.Expiration)
  def userId: StrField       = fields.field(BaseModel.UserId)
  def cc: Seq[StrField]      = fields.field(BaseModel.CC)
  def priority: IntField     = fields.field(BaseModel.Priority)
  def deliveryMode: IntField = fields.field(BaseModel.DeliveryMode)
  def mandatory: BoolField   = fields.field(BaseModel.Mandatory)
  def bcc: Seq[StrField]     = fields.field(BaseModel.BCC)
  def timestamp: BoolField   = fields.field(BaseModel.Timestamp)
  def ack: BoolField         = fields.field(BaseModel.Ack)

  def withExpiration(expiration: Int): this.type     = set(BaseModel.Expiration, expiration)
  def withUserId(userId: String): this.type          = set(BaseModel.UserId, userId)
  def withCc(cC: Seq[String]): this.type             = set(BaseModel.CC, cC)
  def withPriority(priority: Int): this.type         = set(BaseModel.Priority, priority)
  def withDeliveryMode(deliveryMode: Int): this.type = set(BaseModel.DeliveryMode, deliveryMode)
  def withMandatory(mandatory: Boolean): this.type   = set(BaseModel.Mandatory, mandatory)
  def withBcc(bCC: Seq[String]): this.type           = set(BaseModel.BCC, bCC)
  def withTimestamp(timestamp: Boolean): this.type   = set(BaseModel.Timestamp, timestamp)
  def withAck(ack: Boolean): this.type               = set(BaseModel.Ack, ack)
}

class Amqp091OperationBinding010(override val fields: Fields, override val annotations: Annotations)
    extends Amqp091OperationBinding(fields, annotations) {

  def replyTo: StrField                       = fields.field(Amqp010Model.ReplyTo)
  def withReplyTo(replyTo: String): this.type = set(Amqp010Model.ReplyTo, replyTo)

  override def meta: Amqp010Model.type = Amqp010Model

  override def componentId: String = "/amqp091-operation-010"

  override def linkCopy(): Amqp091OperationBinding010 = Amqp091OperationBinding010().withId(id)

  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement =
    Amqp091OperationBinding010.apply
}

object Amqp091OperationBinding010 {

  def apply(): Amqp091OperationBinding010 = apply(Annotations())

  def apply(annotations: Annotations): Amqp091OperationBinding010 = apply(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): Amqp091OperationBinding010 =
    new Amqp091OperationBinding010(fields, annotations)
}

class Amqp091OperationBinding030(override val fields: Fields, override val annotations: Annotations)
    extends Amqp091OperationBinding(fields, annotations) {
  override def key: StrField = fields.field(BaseModel.key)

  override def meta: Amqp030Model.type = Amqp030Model

  override def componentId: String = "/amqp091-operation-030"

  override def linkCopy(): Amqp091OperationBinding030 = Amqp091OperationBinding030().withId(id)

  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement =
    Amqp091OperationBinding030.apply
}

object Amqp091OperationBinding030 {

  def apply(): Amqp091OperationBinding030 = apply(Annotations())

  def apply(annotations: Annotations): Amqp091OperationBinding030 = apply(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): Amqp091OperationBinding030 =
    new Amqp091OperationBinding030(fields, annotations)
}
