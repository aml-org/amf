package amf.apicontract.client.scala.model.domain.bindings.anypointmq

import amf.apicontract.client.scala.model.domain.bindings.{BindingVersion, MessageBinding, BindingHeaders}
import amf.apicontract.internal.metamodel.domain.bindings.AnypointMQMessageBindingModel
import amf.apicontract.internal.metamodel.domain.bindings.AnypointMQMessageBindingModel._
import amf.apicontract.internal.spec.async.parser.bindings.Bindings.AnypointMQ
import amf.core.client.scala.model.domain.{DomainElement, Linkable}
import amf.core.client.scala.model.StrField
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.shapes.client.scala.model.domain.Key

class AnypointMQMessageBinding(override val fields: Fields, override val annotations: Annotations)
    extends MessageBinding
    with BindingVersion
    with BindingHeaders
    with Key {

  override protected def bindingVersionField: Field = BindingVersion
  override protected def headersField: Field        = Headers

  override def meta: AnypointMQMessageBindingModel.type = AnypointMQMessageBindingModel

  override def key: StrField = fields.field(AnypointMQMessageBindingModel.key)

  override def componentId: String = s"/$AnypointMQ-message"

  override def linkCopy(): AnypointMQMessageBinding = AnypointMQMessageBinding().withId(id)

  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement =
    AnypointMQMessageBinding.apply
}

object AnypointMQMessageBinding {

  def apply(): AnypointMQMessageBinding = apply(Annotations())

  def apply(annotations: Annotations): AnypointMQMessageBinding = apply(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): AnypointMQMessageBinding =
    new AnypointMQMessageBinding(fields, annotations)
}
