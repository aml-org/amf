package amf.apicontract.client.scala.model.domain.bindings.pulsar

import amf.apicontract.client.scala.model.domain.bindings.{BindingVersion, ServerBinding}
import amf.apicontract.internal.metamodel.domain.bindings.PulsarServerBindingModel
import amf.apicontract.internal.metamodel.domain.bindings.PulsarServerBindingModel._
import amf.apicontract.internal.spec.async.parser.bindings.Bindings.Pulsar
import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.domain.{DomainElement, Linkable}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.shapes.client.scala.model.domain.Key

class PulsarServerBinding(override val fields: Fields, override val annotations: Annotations)
    extends ServerBinding
    with BindingVersion
    with Key {
  override protected def bindingVersionField: Field = BindingVersion
  override def meta: PulsarServerBindingModel.type  = PulsarServerBindingModel

  def tenant: StrField                      = fields.field(Tenant)
  def withTenant(tenant: String): this.type = set(Tenant, tenant)

  override def componentId: String             = s"/$Pulsar-server"
  override def linkCopy(): PulsarServerBinding = PulsarServerBinding().withId(id)

  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement =
    PulsarServerBinding.apply

  override def key: StrField = fields.field(PulsarServerBindingModel.key)
}

object PulsarServerBinding {

  def apply(): PulsarServerBinding = apply(Annotations())

  def apply(annotations: Annotations): PulsarServerBinding = apply(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): PulsarServerBinding =
    new PulsarServerBinding(fields, annotations)
}
