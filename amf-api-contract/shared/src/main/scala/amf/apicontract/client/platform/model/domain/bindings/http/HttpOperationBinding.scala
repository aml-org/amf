package amf.apicontract.client.platform.model.domain.bindings.http

import amf.apicontract.client.platform.model.domain.bindings.{BindingVersion, OperationBinding}
import amf.core.client.platform.model.StrField
import amf.core.client.platform.model.domain.Shape
import amf.apicontract.client.scala.model.domain.bindings.http.{
  HttpOperationBinding => InternalHttpOperationBinding,
  HttpOperationBinding010 => InternalHttpOperationBinding010
}
import amf.apicontract.internal.convert.ApiClientConverters._

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
abstract class HttpOperationBinding(override private[amf] val _internal: InternalHttpOperationBinding)
    extends OperationBinding
    with BindingVersion {

  def method: StrField = _internal.method
  def query: Shape     = _internal.query

  def withMethod(method: String): this.type = {
    _internal.withMethod(method)
    this
  }
  def withQuery(query: Shape): this.type = {
    _internal.withQuery(query)
    this
  }
  override protected def bindingVersion: StrField = _internal.bindingVersion
  override def withBindingVersion(bindingVersion: String): this.type = {
    _internal.withBindingVersion(bindingVersion)
    this
  }
}

@JSExportAll
case class HttpOperationBinding010(override private[amf] val _internal: InternalHttpOperationBinding010)
    extends HttpOperationBinding(_internal) {
  @JSExportTopLevel("HttpOperationBinding010")
  def this() = this(InternalHttpOperationBinding010())

  def operationType: StrField = _internal.operationType

  def withOperationType(`type`: String): this.type = {
    _internal.withOperationType(`type`)
    this
  }

  override def linkCopy(): HttpOperationBinding010 = _internal.linkCopy()
}
