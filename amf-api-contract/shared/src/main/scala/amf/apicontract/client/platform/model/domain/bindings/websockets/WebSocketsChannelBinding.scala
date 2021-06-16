package amf.apicontract.client.platform.model.domain.bindings.websockets

import amf.apicontract.client.platform.model.domain.bindings.{BindingVersion, ChannelBinding}
import amf.core.client.platform.model.StrField
import amf.core.client.platform.model.domain.Shape
import amf.apicontract.client.scala.model.domain.bindings.websockets.{WebSocketsChannelBinding => InternalWebSocketsChannelBinding}
import amf.apicontract.internal.convert.ApiClientConverters._

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

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
