package amf.apicontract.client.platform.model.domain

import amf.apicontract.internal.convert.ApiClientConverters.ClientList
import amf.core.client.platform.model.StrField
import amf.core.client.platform.model.domain.{DomainElement, Linkable, NamedDomainElement}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * Channel bindings model class.
  */
@JSExportAll
case class ChannelBindings(override private[amf] val _internal: InternalChannelBindings)
    extends DomainElement
    with NamedDomainElement
    with Linkable {

  @JSExportTopLevel("model.domain.ChannelBindings")
  def this() = this(InternalChannelBindings())

  def name: StrField                       = _internal.name
  def bindings: ClientList[ChannelBinding] = _internal.bindings.asClient

  /** Set name property of ChannelBindings. */
  def withName(name: String): this.type = {
    _internal.withName(name)
    this
  }

  def withBindings(bindings: ClientList[ChannelBinding]): this.type = {
    _internal.withBindings(bindings.asInternal)
    this
  }

  override def linkCopy(): ChannelBindings = _internal.linkCopy()
}
