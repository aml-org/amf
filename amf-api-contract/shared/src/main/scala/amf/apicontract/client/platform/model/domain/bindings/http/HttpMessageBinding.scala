package amf.apicontract.client.platform.model.domain.bindings.http

import amf.apicontract.client.platform.model.domain.bindings.{BindingHeaders, BindingVersion, MessageBinding}
import amf.core.client.platform.model.{IntField, StrField}
import amf.core.client.platform.model.domain.Shape
import amf.apicontract.client.scala.model.domain.bindings.http.{
  HttpMessageBinding => InternalHttpMessageBinding,
  HttpMessageBinding020 => InternalHttpMessageBinding020,
  HttpMessageBinding030 => InternalHttpMessageBinding030
}
import amf.apicontract.internal.convert.ApiClientConverters._

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
abstract class HttpMessageBinding(override private[amf] val _internal: InternalHttpMessageBinding)
    extends MessageBinding
    with BindingVersion
    with BindingHeaders {

  override protected def bindingVersion: StrField = _internal.bindingVersion
  override def withBindingVersion(bindingVersion: String): this.type = {
    _internal.withBindingVersion(bindingVersion)
    this
  }

  override def headers: Shape = _internal.headers

  override def withHeaders(headers: Shape): this.type = {
    _internal.withHeaders(headers)
    this
  }
}

@JSExportAll
case class HttpMessageBinding020(override private[amf] val _internal: InternalHttpMessageBinding020)
    extends HttpMessageBinding(_internal) {
  @JSExportTopLevel("HttpMessageBinding020")
  def this() = this(InternalHttpMessageBinding020())
  override def linkCopy(): HttpMessageBinding020 = _internal.linkCopy()
}

@JSExportAll
case class HttpMessageBinding030(override private[amf] val _internal: InternalHttpMessageBinding030)
    extends HttpMessageBinding(_internal) {
  @JSExportTopLevel("HttpMessageBinding030")
  def this() = this(InternalHttpMessageBinding030())

  def statusCode: IntField = _internal.statusCode
  def withStatusCode(statusCode: Int): this.type = {
    _internal.withStatusCode(statusCode)
    this
  }

  override def linkCopy(): HttpMessageBinding030 = _internal.linkCopy()
}
