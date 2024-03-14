package amf.apicontract.client.scala.model.domain.bindings.solace

import amf.apicontract.client.scala.model.domain.bindings.{BindingVersion, ServerBinding}
import amf.apicontract.internal.metamodel.domain.bindings.SolaceServerBindingModel._
import amf.apicontract.internal.metamodel.domain.bindings.SolaceServerBindingModel
import amf.apicontract.internal.spec.async.parser.bindings.Bindings.Solace
import amf.core.client.scala.model.domain.{DomainElement, Linkable}
import amf.core.client.scala.model.StrField
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.shapes.client.scala.model.domain.Key

class SolaceServerBinding(override val fields: Fields, override val annotations: Annotations)
    extends ServerBinding
    with BindingVersion
    with Key {
  override protected def bindingVersionField: Field = BindingVersion
  override def meta: SolaceServerBindingModel.type  = SolaceServerBindingModel

  def msgVpn: StrField                      = fields.field(MsgVpn)
  def withMsgVpn(msgVpn: String): this.type = set(MsgVpn, msgVpn)

  override def componentId: String             = s"/$Solace-server"
  override def linkCopy(): SolaceServerBinding = SolaceServerBinding().withId(id)

  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement =
    SolaceServerBinding.apply

  override def key: StrField = fields.field(SolaceServerBindingModel.key)
}

object SolaceServerBinding {

  def apply(): SolaceServerBinding = apply(Annotations())

  def apply(annotations: Annotations): SolaceServerBinding = apply(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): SolaceServerBinding =
    new SolaceServerBinding(fields, annotations)
}
