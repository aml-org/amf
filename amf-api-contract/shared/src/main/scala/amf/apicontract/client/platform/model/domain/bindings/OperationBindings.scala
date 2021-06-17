package amf.apicontract.client.platform.model.domain.bindings

import amf.core.client.platform.model.StrField
import amf.core.client.platform.model.domain.{DomainElement, Linkable, NamedDomainElement}
import amf.apicontract.client.scala.model.domain.bindings.{OperationBindings => InternalOperationBindings}
import amf.apicontract.internal.convert.ApiClientConverters._
import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * Operation channel model class.
  */
@JSExportAll
case class OperationBindings(override private[amf] val _internal: InternalOperationBindings)
    extends DomainElement
    with NamedDomainElement
    with Linkable {

  @JSExportTopLevel("OperationBindings")
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
