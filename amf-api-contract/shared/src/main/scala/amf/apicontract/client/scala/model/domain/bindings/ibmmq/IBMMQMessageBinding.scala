package amf.apicontract.client.scala.model.domain.bindings.ibmmq

import amf.apicontract.client.scala.model.domain.bindings.{BindingVersion, MessageBinding}
import amf.apicontract.internal.metamodel.domain.bindings.IBMMQMessageBindingModel
import amf.apicontract.internal.metamodel.domain.bindings.IBMMQMessageBindingModel._
import amf.apicontract.internal.spec.async.parser.bindings.Bindings.IBMMQ
import amf.core.client.scala.model.{IntField, StrField}
import amf.core.client.scala.model.domain.{DomainElement, Linkable}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.shapes.client.scala.model.domain.Key

class IBMMQMessageBinding(override val fields: Fields, override val annotations: Annotations)
    extends MessageBinding
    with BindingVersion
    with Key {

  override protected def bindingVersionField: Field = BindingVersion
  override def meta: IBMMQMessageBindingModel.type  = IBMMQMessageBindingModel

  def messageType: StrField = fields.field(MessageType)
  def headers: StrField     = fields.field(Headers)
  def description: StrField = fields.field(Description)
  def expiry: IntField      = fields.field(Expiry)

  def withMessageType(messageType: String): this.type = set(MessageType, messageType)
  def withHeaders(headers: String): this.type         = set(Headers, headers)
  def withDescription(description: String): this.type = set(Description, description)
  def withExpiry(expiry: Int): this.type              = set(Expiry, expiry)

  override def key: StrField = fields.field(IBMMQMessageBindingModel.key)

  override def componentId: String = s"/$IBMMQ-message"

  override def linkCopy(): IBMMQMessageBinding = IBMMQMessageBinding()

  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement =
    IBMMQMessageBinding.apply
}

object IBMMQMessageBinding {

  def apply(): IBMMQMessageBinding = apply(Annotations())

  def apply(annotations: Annotations): IBMMQMessageBinding = apply(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): IBMMQMessageBinding =
    new IBMMQMessageBinding(fields, annotations)
}
