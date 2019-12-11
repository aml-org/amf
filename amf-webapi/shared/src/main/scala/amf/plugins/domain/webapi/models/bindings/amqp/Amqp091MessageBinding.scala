package amf.plugins.domain.webapi.models.bindings.amqp
import amf.core.metamodel.{Field, Obj}
import amf.core.model.StrField
import amf.core.model.domain.{Linkable, DomainElement}
import amf.core.parser.{Annotations, Fields}
import amf.plugins.domain.webapi.metamodel.bindings.Amqp091MessageBindingModel
import amf.plugins.domain.webapi.metamodel.bindings.Amqp091MessageBindingModel._
import amf.plugins.domain.webapi.models.bindings.{BindingVersion, MessageBinding}

class Amqp091MessageBinding(override val fields: Fields, override val annotations: Annotations)
    extends MessageBinding
    with BindingVersion {
  def contentEncoding: StrField                     = fields.field(ContentEncoding)
  def messageType: StrField                         = fields.field(MessageType)
  override protected def bindingVersionField: Field = BindingVersion
  override def meta: Obj                            = Amqp091MessageBindingModel

  def withContentEncoding(contentEncoding: String): this.type = set(ContentEncoding, contentEncoding)
  def withMessageType(messageType: String): this.type         = set(MessageType, messageType)

  override def componentId: String = "/amqp091-message"
  override def linkCopy(): Amqp091MessageBinding = Amqp091MessageBinding().withId(id)

  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement = Amqp091MessageBinding.apply
}

object Amqp091MessageBinding {

  def apply(): Amqp091MessageBinding = apply(Annotations())

  def apply(annotations: Annotations): Amqp091MessageBinding = apply(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): Amqp091MessageBinding = new Amqp091MessageBinding(fields, annotations)
}
