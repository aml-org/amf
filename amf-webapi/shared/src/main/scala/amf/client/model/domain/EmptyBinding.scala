package amf.client.model.domain

import amf.client.convert.WebApiClientConverters._
import amf.client.model.StrField
import amf.plugins.domain.webapi.models.bindings.{EmptyBinding => InternalEmptyBinding}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class EmptyBinding(override private[amf] val _internal: InternalEmptyBinding)
    extends ServerBinding
    with OperationBinding
    with ChannelBinding
    with MessageBinding {

  @JSExportTopLevel("model.domain.EmptyBinding")
  def this() = this(InternalEmptyBinding())

  def `type`: StrField = _internal.`type`

  def withType(`type`: String): this.type = {
    _internal.withType(`type`)
    this
  }

  override def linkCopy(): EmptyBinding = _internal.linkCopy()
}
