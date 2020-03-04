package amf.client.model.domain
import amf.client.model.StrField
import amf.plugins.domain.webapi.models.bindings.{
  ChannelBinding => InternalChannelBinding,
  OperationBinding => InternalOperationBinding,
  MessageBinding => InternalMessageBinding,
  ServerBinding => InternalServerBinding
}
import amf.client.convert.WebApiClientConverters._
import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
trait ChannelBinding extends DomainElement with NamedDomainElement with Linkable {
  override private[amf] val _internal: InternalChannelBinding
  def name: StrField = _internal.name
  def withName(name: String): this.type = {
    _internal.withName(name)
    this
  }
}

@JSExportAll
trait OperationBinding extends DomainElement with NamedDomainElement with Linkable {
  override private[amf] val _internal: InternalOperationBinding
  def name: StrField = _internal.name
  def withName(name: String): this.type = {
    _internal.withName(name)
    this
  }
}

@JSExportAll
trait MessageBinding extends DomainElement with NamedDomainElement with Linkable {
  override private[amf] val _internal: InternalMessageBinding
  def name: StrField = _internal.name
  def withName(name: String): this.type = {
    _internal.withName(name)
    this
  }
}

@JSExportAll
trait ServerBinding extends DomainElement with NamedDomainElement with Linkable {
  override private[amf] val _internal: InternalServerBinding
  def name: StrField = _internal.name
  def withName(name: String): this.type = {
    _internal.withName(name)
    this
  }
}

trait BindingVersion {
  protected def bindingVersion: StrField
  def withBindingVersion(bindingVersion: String): this.type

}
