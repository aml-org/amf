package amf.apicontract.client.scala.model.domain.bindings
import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.domain.DomainElement
import amf.core.internal.metamodel.Field

trait BindingVersion extends DomainElement {

  protected def bindingVersionField: Field

  def bindingVersion: StrField = fields.field(bindingVersionField)

  def withBindingVersion(version: String): this.type =
    set(bindingVersionField, version)

}
