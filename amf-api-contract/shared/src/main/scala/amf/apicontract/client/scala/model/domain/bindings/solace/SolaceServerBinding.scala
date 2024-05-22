package amf.apicontract.client.scala.model.domain.bindings.solace

import amf.apicontract.client.scala.model.domain.bindings.{BindingVersion, ServerBinding}
import amf.apicontract.internal.metamodel.domain.bindings.SolaceServerBindingModel._
import amf.apicontract.internal.metamodel.domain.bindings.{SolaceServerBinding010Model, SolaceServerBinding040Model, SolaceServerBindingModel}
import amf.apicontract.internal.spec.async.parser.bindings.Bindings.Solace
import amf.core.client.scala.model.domain.{DomainElement, Linkable}
import amf.core.client.scala.model.StrField
import amf.core.internal.metamodel.{Field, Obj}
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.shapes.client.scala.model.domain.Key

abstract class SolaceServerBinding(override val fields: Fields, override val annotations: Annotations)
    extends ServerBinding
    with BindingVersion
    with Key {
  override protected def bindingVersionField: Field = BindingVersion
  def msgVpn: StrField                      = fields.field(MsgVpn)
  def withMsgVpn(msgVpn: String): this.type = set(MsgVpn, msgVpn)
  override def componentId: String             = s"/$Solace-server"
  override def key: StrField = fields.field(SolaceServerBindingModel.key)
}
class SolaceServerBinding010(override val fields: Fields, override val annotations: Annotations)
    extends SolaceServerBinding(fields, annotations) {
  /** apply method for create a new instance with fields and annotations. Aux method for copy */
  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement = ???
  override def componentId: String = s"/$Solace-server-binding-010"
  override def meta: SolaceServerBinding010Model.type = SolaceServerBinding010Model
  override def linkCopy(): SolaceServerBinding = SolaceServerBinding010().withId(id)
}
object SolaceServerBinding010 {

  def apply(): SolaceServerBinding010 = apply(Annotations())

  def apply(annotations: Annotations): SolaceServerBinding010 = apply(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): SolaceServerBinding010 =
    new SolaceServerBinding010(fields, annotations)
}
class SolaceServerBinding040(override val fields: Fields, override val annotations: Annotations)
    extends SolaceServerBinding(fields, annotations) {
  def clientName: StrField = fields.field(SolaceServerBinding040Model.ClientName)
  def withClientName(clientName: String): this.type = set(SolaceServerBinding040Model.ClientName, clientName)
  override def meta: SolaceServerBinding040Model.type = SolaceServerBinding040Model
  override def componentId: String = s"/$Solace-server-binding-040"

  /** apply method for create a new instance with fields and annotations. Aux method for copy */
  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement = SolaceServerBinding040.apply
  override def linkCopy(): SolaceServerBinding = SolaceServerBinding040().withId(id)
}
object SolaceServerBinding040 {

  def apply(): SolaceServerBinding040 = apply(Annotations())

  def apply(annotations: Annotations): SolaceServerBinding040 = apply(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): SolaceServerBinding040 =
    new SolaceServerBinding040(fields, annotations)
}
