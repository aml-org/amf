package amf.apicontract.client.platform.model.domain.bindings

import amf.core.client.platform.model.StrField
import amf.core.client.platform.model.domain.{DomainElement, Linkable, NamedDomainElement}
import amf.apicontract.client.scala.model.domain.bindings.{ServerBindings => InternalServerBindings}
import amf.apicontract.internal.convert.ApiClientConverters._
import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * Server channel model class.
  */
@JSExportAll
case class ServerBindings(override private[amf] val _internal: InternalServerBindings)
    extends DomainElement
    with NamedDomainElement
    with Linkable {

  @JSExportTopLevel("ServerBindings")
  def this() = this(InternalServerBindings())

  def name: StrField                      = _internal.name
  def bindings: ClientList[ServerBinding] = _internal.bindings.asClient

  /** Set name property of ServerBindings. */
  def withName(name: String): this.type = {
    _internal.withName(name)
    this
  }

  def withBindings(bindings: ClientList[ServerBinding]): this.type = {
    _internal.withBindings(bindings.asInternal)
    this
  }

  override def linkCopy(): ServerBindings = _internal.linkCopy()
}
