package amf.plugins.domain.webapi.models.bindings.amqp
import amf.plugins.domain.webapi.metamodel.bindings.Amqp091OperationBindingModel
import amf.plugins.domain.webapi.models.bindings.{OperationBinding, BindingVersion}
import amf.core.model.{BoolField, IntField, StrField}
import amf.core.metamodel.{Field, Obj}
import amf.core.model.domain.{Linkable, DomainElement}
import amf.core.parser.{Annotations, Fields}
import amf.plugins.domain.webapi.metamodel.bindings.Amqp091OperationBindingModel._

class Amqp091OperationBinding(override val fields: Fields, override val annotations: Annotations)
    extends OperationBinding
    with BindingVersion {
  override protected def bindingVersionField: Field = BindingVersion
  override def meta: Obj                            = Amqp091OperationBindingModel

  override def componentId: String = "Amqp091OperationBinding"
  def expiration: IntField         = fields.field(Expiration)
  def userId: StrField             = fields.field(UserId)
  def cc: Seq[StrField]            = fields.field(CC)
  def priority: IntField           = fields.field(Priority)
  def deliveryMode: IntField       = fields.field(DeliveryMode)
  def mandatory: BoolField         = fields.field(Mandatory)
  def bcc: Seq[StrField]           = fields.field(BCC)
  def replyTo: StrField            = fields.field(ReplyTo)
  def timestamp: BoolField         = fields.field(Timestamp)
  def ack: BoolField               = fields.field(Ack)

  def withExpiration(expiration: Int): this.type     = set(Expiration, expiration)
  def withUserId(userId: String): this.type          = set(UserId, userId)
  def withCc(cC: Seq[String]): this.type             = set(CC, cC)
  def withPriority(priority: Int): this.type         = set(Priority, priority)
  def withDeliveryMode(deliveryMode: Int): this.type = set(DeliveryMode, deliveryMode)
  def withMandatory(mandatory: Boolean): this.type   = set(Mandatory, mandatory)
  def withBcc(bCC: Seq[String]): this.type           = set(BCC, bCC)
  def withReplyTo(replyTo: String): this.type        = set(ReplyTo, replyTo)
  def withTimestamp(timestamp: Boolean): this.type   = set(Timestamp, timestamp)
  def withAck(ack: Boolean): this.type               = set(Ack, ack)

  override def linkCopy(): Amqp091OperationBinding = Amqp091OperationBinding().withId(id)

  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement = Amqp091OperationBinding.apply
}

object Amqp091OperationBinding {

  def apply(): Amqp091OperationBinding = apply(Annotations())

  def apply(annotations: Annotations): Amqp091OperationBinding = apply(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): Amqp091OperationBinding = new Amqp091OperationBinding(fields, annotations)
}
