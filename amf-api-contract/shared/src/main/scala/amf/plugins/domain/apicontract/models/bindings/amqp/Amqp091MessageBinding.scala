package amf.plugins.domain.apicontract.models.bindings.amqp
import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.domain.{DomainElement, Linkable}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.plugins.domain.apicontract.metamodel.bindings.Amqp091MessageBindingModel
import amf.plugins.domain.apicontract.metamodel.bindings.Amqp091MessageBindingModel._
import amf.plugins.domain.apicontract.models.Key
import amf.plugins.domain.apicontract.models.bindings.{BindingVersion, MessageBinding}

class Amqp091MessageBinding(override val fields: Fields, override val annotations: Annotations)
    extends MessageBinding
    with BindingVersion
    with Key {

  def contentEncoding: StrField                      = fields.field(ContentEncoding)
  def messageType: StrField                          = fields.field(MessageType)
  override protected def bindingVersionField: Field  = BindingVersion
  override def meta: Amqp091MessageBindingModel.type = Amqp091MessageBindingModel

  override def key: StrField = fields.field(Amqp091MessageBindingModel.key)

  def withContentEncoding(contentEncoding: String): this.type = set(ContentEncoding, contentEncoding)
  def withMessageType(messageType: String): this.type         = set(MessageType, messageType)

  override def componentId: String               = "/amqp091-message"
  override def linkCopy(): Amqp091MessageBinding = Amqp091MessageBinding().withId(id)

  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement =
    Amqp091MessageBinding.apply
}

object Amqp091MessageBinding {

  def apply(): Amqp091MessageBinding = apply(Annotations())

  def apply(annotations: Annotations): Amqp091MessageBinding = apply(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): Amqp091MessageBinding =
    new Amqp091MessageBinding(fields, annotations)
}
