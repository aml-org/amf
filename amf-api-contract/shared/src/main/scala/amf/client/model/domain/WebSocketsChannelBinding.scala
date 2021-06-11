package amf.client.model.domain
import amf.client.convert.ApiClientConverters._
import amf.core.client.platform.model.StrField
import amf.core.client.platform.model.domain.Shape

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}
import amf.plugins.domain.apicontract.models.bindings.websockets.{
  WebSocketsChannelBinding => InternalWebSocketsChannelBinding
}

@JSExportAll
case class WebSocketsChannelBinding(override private[amf] val _internal: InternalWebSocketsChannelBinding)
    extends ChannelBinding
    with BindingVersion {
  @JSExportTopLevel("model.domain.WebSocketsChannelBinding")
  def this() = this(InternalWebSocketsChannelBinding())

  override protected def bindingVersion: StrField = _internal.bindingVersion
  override def withBindingVersion(bindingVersion: String): this.type = {
    _internal.withBindingVersion(bindingVersion)
    this
  }

  def method: StrField = _internal.method
  def query: Shape     = _internal.query
  def headers: Shape   = _internal.headers
  def `type`: StrField = _internal.`type`

  def withMethod(method: String): this.type = {
    _internal.withMethod(method)
    this
  }

  def withQuery(query: Shape): this.type = {
    _internal.withQuery(query)
    this
  }

  def withHeaders(headers: Shape): this.type = {
    _internal.withHeaders(headers)
    this
  }

  def withType(`type`: String): this.type = {
    _internal.withType(`type`)
    this
  }

  override def linkCopy(): WebSocketsChannelBinding = _internal.linkCopy()
}
