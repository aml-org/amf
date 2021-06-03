package amf.client.model.domain
import amf.client.convert.ApiClientConverters._

import amf.client.model.StrField

import scala.scalajs.js.annotation.{JSExportTopLevel, JSExportAll}
import amf.plugins.domain.apicontract.models.bindings.http.{HttpOperationBinding => InternalHttpOperationBinding}

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
