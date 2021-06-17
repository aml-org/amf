package amf.apicontract.client.platform.model.domain.bindings

import amf.core.client.platform.model.StrField
import amf.apicontract.client.scala.model.domain.bindings.{EmptyBinding => InternalEmptyBinding}
import amf.apicontract.internal.convert.ApiClientConverters._

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class EmptyBinding(override private[amf] val _internal: InternalEmptyBinding)
    extends ServerBinding
    with OperationBinding
    with ChannelBinding
    with MessageBinding {

  @JSExportTopLevel("EmptyBinding")
  def this() = this(InternalEmptyBinding())

  def `type`: StrField = _internal.`type`

  def withType(`type`: String): this.type = {
    _internal.withType(`type`)
    this
  }

  override def linkCopy(): EmptyBinding = _internal.linkCopy()
}
