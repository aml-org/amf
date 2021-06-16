package amf.apicontract.client.scala.model.domain.bindings
import amf.core.client.scala.model.domain.{DomainElement, Linkable}

trait ChannelBinding   extends DomainElement with Linkable
trait OperationBinding extends DomainElement with Linkable
trait MessageBinding   extends DomainElement with Linkable
trait ServerBinding    extends DomainElement with Linkable
