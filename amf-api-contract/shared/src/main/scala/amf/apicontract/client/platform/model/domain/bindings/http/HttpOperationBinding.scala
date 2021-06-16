package amf.apicontract.client.platform.model.domain.bindings.http

import amf.apicontract.client.platform.model.domain.bindings.{BindingVersion, OperationBinding}
import amf.core.client.platform.model.StrField
import amf.core.client.platform.model.domain.Shape
import amf.apicontract.client.scala.model.domain.bindings.http.{HttpOperationBinding => InternalHttpOperationBinding}
import amf.apicontract.internal.convert.ApiClientConverters._

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class HttpOperationBinding(override private[amf] val _internal: InternalHttpOperationBinding)
    extends OperationBinding
    with BindingVersion {
  @JSExportTopLevel("model.domain.HttpOperationBinding")
  def this() = this(InternalHttpOperationBinding())

  def operationType: StrField = _internal.operationType
  def method: StrField        = _internal.method
  def query: Shape            = _internal.query

  def withOperationType(`type`: String): this.type = {
    _internal.withOperationType(`type`)
    this
  }
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

  override def linkCopy(): HttpOperationBinding = _internal.linkCopy()
}
