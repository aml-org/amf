package amf.apicontract.client.platform.model.domain

import amf.core.client.platform.model.StrField
import amf.core.client.platform.model.domain.DomainElement

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * Scope model class.
  */
@JSExportAll
case class Scope(override private[amf] val _internal: InternalScope) extends DomainElement {

  @JSExportTopLevel("model.domain.Scope")
  def this() = this(InternalScope())

  def name: StrField        = _internal.name
  def description: StrField = _internal.description

  /** Set name property of this Scope. */
  def withName(name: String): this.type = {
    _internal.withName(name)
    this
  }

  /** Set description property of this Scope] */
  def withDescription(description: String): this.type = {
    _internal.withDescription(description)
    this
  }
}
