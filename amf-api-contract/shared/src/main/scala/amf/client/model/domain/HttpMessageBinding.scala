package amf.client.model.domain
import amf.client.convert.ApiClientConverters._
import amf.core.client.platform.model.StrField
import amf.core.client.platform.model.domain.Shape

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}
import amf.plugins.domain.apicontract.models.bindings.http.{HttpMessageBinding => InternalHttpMessageBinding}

@JSExportAll
case class HttpMessageBinding(override private[amf] val _internal: InternalHttpMessageBinding)
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
