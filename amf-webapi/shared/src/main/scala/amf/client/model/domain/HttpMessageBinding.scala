package amf.client.model.domain
import amf.client.convert.WebApiClientConverters._
import amf.client.model.StrField

import scala.scalajs.js.annotation.{JSExportTopLevel, JSExportAll}
import amf.plugins.domain.webapi.models.bindings.http.{HttpMessageBinding => InternalHttpMessageBinding}

@JSExportAll
class HttpMessageBinding(override private[amf] val _internal: InternalHttpMessageBinding)
    extends MessageBinding
    with BindingVersion {
  @JSExportTopLevel("model.domain.HttpMessageBinding")
  def this() = this(InternalHttpMessageBinding())
  override protected def bindingVersion: StrField = _internal.bindingVersion
  override def withBindingVersion(bindingVersion: String): this.type = {
    _internal.withBindingVersion(bindingVersion)
    this
  }

  def headers: Shape = _internal.headers

  def withHeaders(headers: Shape): this.type = {
    _internal.withHeaders(headers)
    this
  }

  override def linkCopy(): HttpMessageBinding = _internal.linkCopy()
}
