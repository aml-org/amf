package amf.apicontract.client.scala.model.domain.bindings.anypointmq

import amf.apicontract.client.scala.model.domain.bindings.{BindingVersion, ChannelBinding}
import amf.apicontract.internal.metamodel.domain.bindings.AnypointMQChannelBindingModel
import amf.apicontract.internal.metamodel.domain.bindings.AnypointMQChannelBindingModel._
import amf.apicontract.internal.spec.async.parser.bindings.Bindings.AnypointMQ
import amf.core.client.scala.model.domain.{DomainElement, Linkable}
import amf.core.client.scala.model.StrField
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.shapes.client.scala.model.domain.Key

class AnypointMQChannelBinding(override val fields: Fields, override val annotations: Annotations)
    extends ChannelBinding
    with BindingVersion
    with Key {

  override protected def bindingVersionField: Field     = BindingVersion
  override def meta: AnypointMQChannelBindingModel.type = AnypointMQChannelBindingModel

  def destination: StrField     = fields.field(Destination)
  def destinationType: StrField = fields.field(DestinationType)

  def withDestination(destination: String): this.type         = set(Destination, destination)
  def withDestinationType(destinationType: String): this.type = set(DestinationType, destinationType)

  override def key: StrField = fields.field(AnypointMQChannelBindingModel.key)

  override def componentId: String = s"/$AnypointMQ-channel"

  override def linkCopy(): AnypointMQChannelBinding = AnypointMQChannelBinding().withId(id)

  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement =
    AnypointMQChannelBinding.apply
}

object AnypointMQChannelBinding {

  def apply(): AnypointMQChannelBinding = apply(Annotations())

  def apply(annotations: Annotations): AnypointMQChannelBinding = apply(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): AnypointMQChannelBinding =
    new AnypointMQChannelBinding(fields, annotations)
}
