package amf.client.model.domain

import amf.client.convert.WebApiClientConverters._
import amf.client.model.StrField

import scala.scalajs.js.annotation.{JSExportTopLevel, JSExportAll}
import amf.plugins.domain.webapi.models.bindings.websockets.{
  WebSocketsChannelBinding => InternalWebSocketsChannelBinding
}

@JSExportAll
class WebSocketsChannelBinding(override private[amf] val _internal: InternalWebSocketsChannelBinding)
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

  override def linkCopy(): WebSocketsChannelBinding = _internal.linkCopy()
}
