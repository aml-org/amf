package amf.client.model.domain

import amf.client.model.StrField
import amf.plugins.domain.webapi.models.bindings.{DynamicBinding => InternalDynamicBinding}
import amf.plugins.domain.webapi.models.bindings.{EmptyBinding => InternalEmptyBinding}

import amf.client.convert.WebApiClientConverters._
import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class DynamicBinding(override private[amf] val _internal: InternalDynamicBinding)
    extends ServerBinding
    with OperationBinding
    with ChannelBinding
    with MessageBinding {

  @JSExportTopLevel("model.domain.DynamicBinding")
  def this() = this(InternalDynamicBinding())

  def definition: DataNode = _internal.definition
  def `type`: StrField     = _internal.`type`

  def withDefinition(definition: DataNode): this.type = {
    _internal.withDefinition(definition)
    this
  }

  override def name: StrField = _internal.name

  override def withName(name: String): this.type = {
    _internal.withName(name)
    this
  }

  def withType(`type`: String): this.type = {
    _internal.withType(`type`)
    this
  }
  override def linkCopy(): DynamicBinding = _internal.linkCopy()
}

@JSExportAll
case class EmptyBinding(override private[amf] val _internal: InternalEmptyBinding)
    extends ServerBinding
    with OperationBinding
    with ChannelBinding
    with MessageBinding {

  @JSExportTopLevel("model.domain.EmptyBinding")
  def this() = this(InternalEmptyBinding())

  override def name: StrField = _internal.name

  override def withName(name: String): this.type = {
    _internal.withName(name)
    this
  }

  def `type`: StrField = _internal.`type`

  def withType(`type`: String): this.type = {
    _internal.withType(`type`)
    this
  }

  override def linkCopy(): EmptyBinding = _internal.linkCopy()
}
