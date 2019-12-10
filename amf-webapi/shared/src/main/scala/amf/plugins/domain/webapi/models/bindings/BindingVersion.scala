package amf.plugins.domain.webapi.models.bindings
import amf.core.metamodel.Field
import amf.core.model.StrField
import amf.core.model.domain.DomainElement

trait BindingVersion extends DomainElement {

  protected def bindingVersionField: Field

  def bindingVersion: StrField = fields.field(bindingVersionField)

  def withBindingVersion(version: String): this.type =
    set(bindingVersionField, version)

}
