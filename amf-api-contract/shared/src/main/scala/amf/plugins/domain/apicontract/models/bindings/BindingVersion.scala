package amf.plugins.domain.apicontract.models.bindings
import amf.core.metamodel.Field
import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.domain.DomainElement

trait BindingVersion extends DomainElement {

  protected def bindingVersionField: Field

  def bindingVersion: StrField = fields.field(bindingVersionField)

  def withBindingVersion(version: String): this.type =
    set(bindingVersionField, version)

}
