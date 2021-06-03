package amf.client.model.domain
import amf.client.convert.ApiClientConverters._

import amf.client.model.StrField
import amf.plugins.domain.apicontract.models.security.{Scope => InternalScope}

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
