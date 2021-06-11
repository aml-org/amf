package amf.client.model.domain
import amf.client.convert.ApiClientConverters._

import amf.core.client.platform.model.StrField
import amf.plugins.domain.apicontract.models.bindings.{OperationBindings => InternalOperationBindings}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}
import amf.core.client.platform.model.domain.{DomainElement, Linkable, NamedDomainElement}

/**
  * Operation channel model class.
  */
@JSExportAll
case class OperationBindings(override private[amf] val _internal: InternalOperationBindings)
    extends DomainElement
    with NamedDomainElement
    with Linkable {

  @JSExportTopLevel("model.domain.OperationBindings")
  def this() = this(InternalOperationBindings())

  def name: StrField                         = _internal.name
  def bindings: ClientList[OperationBinding] = _internal.bindings.asClient

  /** Set name property of OperationBindings. */
  def withName(name: String): this.type = {
    _internal.withName(name)
    this
  }

  def withBindings(bindings: ClientList[OperationBinding]): this.type = {
    _internal.withBindings(bindings.asInternal)
    this
  }

  override def linkCopy(): OperationBindings = _internal.linkCopy()
}
