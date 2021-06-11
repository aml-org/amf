package amf.client.model.domain
import amf.client.convert.ApiClientConverters._
import amf.core.client.platform.model.StrField
import amf.core.client.platform.model.domain.Shape

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}
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
