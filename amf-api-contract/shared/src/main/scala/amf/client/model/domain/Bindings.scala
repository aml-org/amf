package amf.client.model.domain
import amf.core.client.platform.model.StrField
import amf.core.client.platform.model.domain.{DomainElement, Linkable}
import amf.plugins.domain.apicontract.models.bindings.{
  ChannelBinding => InternalChannelBinding,
  MessageBinding => InternalMessageBinding,
  OperationBinding => InternalOperationBinding,
  ServerBinding => InternalServerBinding
}

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
trait ChannelBinding extends DomainElement with Linkable {
  override private[amf] val _internal: InternalChannelBinding
}

@JSExportAll
trait OperationBinding extends DomainElement with Linkable {
  override private[amf] val _internal: InternalOperationBinding
}

@JSExportAll
trait MessageBinding extends DomainElement with Linkable {
  override private[amf] val _internal: InternalMessageBinding
}

@JSExportAll
trait ServerBinding extends DomainElement with Linkable {
  override private[amf] val _internal: InternalServerBinding
}

trait BindingVersion {
  protected def bindingVersion: StrField
  def withBindingVersion(bindingVersion: String): this.type

}
