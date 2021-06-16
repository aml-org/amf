package amf.apicontract.client.platform.model.domain.bindings

import amf.core.client.platform.model.StrField
import amf.core.client.platform.model.domain.{DomainElement, Linkable, NamedDomainElement}
import amf.apicontract.client.scala.model.domain.bindings.{MessageBindings => InternalMessageBindings}
import amf.apicontract.internal.convert.ApiClientConverters._
import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * Message channel model class.
  */
@JSExportAll
case class MessageBindings(override private[amf] val _internal: InternalMessageBindings)
    extends DomainElement
    with NamedDomainElement
    with Linkable {

  @JSExportTopLevel("model.domain.MessageBindings")
  def this() = this(InternalMessageBindings())

  def name: StrField                       = _internal.name
  def bindings: ClientList[MessageBinding] = _internal.bindings.asClient

  /** Set name property of MessageBindings. */
  def withName(name: String): this.type = {
    _internal.withName(name)
    this
  }

  def withBindings(bindings: ClientList[MessageBinding]): this.type = {
    _internal.withBindings(bindings.asInternal)
    this
  }

  override def linkCopy(): MessageBindings = _internal.linkCopy()
}
