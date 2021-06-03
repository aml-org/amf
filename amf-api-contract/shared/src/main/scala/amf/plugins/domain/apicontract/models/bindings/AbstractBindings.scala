package amf.plugins.domain.apicontract.models.bindings
import amf.core.model.domain.{DomainElement, Linkable}

trait ChannelBinding   extends DomainElement with Linkable
trait OperationBinding extends DomainElement with Linkable
trait MessageBinding   extends DomainElement with Linkable
trait ServerBinding    extends DomainElement with Linkable
