package amf.model.domain

import amf.plugins.domain.webapi.models.security

import scala.scalajs.js.annotation.JSExportAll

/**
  * JS Scope model class.
  */
@JSExportAll
case class Scope private[model] (private val scope: amf.plugins.domain.webapi.models.security.Scope) extends DomainElement {
  def this() = this(security.Scope())

  val name: String        = scope.name
  val description: String = scope.description

  override private[amf] def element: amf.plugins.domain.webapi.models.security.Scope = scope

  /** Set name property of this [[Scope]]. */
  def withName(name: String): this.type = {
    scope.withName(name)
    this
  }

  /** Set description property of this [[Scope]]. */
  def withDescription(description: String): this.type = {
    scope.withDescription(description)
    this
  }
}
