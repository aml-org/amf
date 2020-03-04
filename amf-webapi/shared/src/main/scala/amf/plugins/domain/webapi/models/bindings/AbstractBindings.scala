package amf.plugins.domain.webapi.models.bindings
import amf.core.metamodel.Field
import amf.core.metamodel.domain.common.NameFieldSchema
import amf.core.model.domain.{Linkable, NamedDomainElement}

trait ChannelBinding extends NamedDomainElement with Linkable with NameFieldSchema {
  override protected def nameField: Field = Name
}
trait OperationBinding extends NamedDomainElement with Linkable with NameFieldSchema {
  override protected def nameField: Field = Name
}
trait MessageBinding extends NamedDomainElement with Linkable with NameFieldSchema {
  override protected def nameField: Field = Name
}
trait ServerBinding extends NamedDomainElement with Linkable with NameFieldSchema {
  override protected def nameField: Field = Name
}
