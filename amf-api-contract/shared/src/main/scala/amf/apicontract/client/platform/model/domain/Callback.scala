package amf.apicontract.client.platform.model.domain

import amf.core.client.platform.model.StrField
import amf.core.client.platform.model.domain.{DomainElement, NamedAmfObject}
import amf.apicontract.internal.convert.ApiClientConverters._

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}
import amf.apicontract.client.scala.model.domain.{Callback => InternalCallback}

/** Callback model class.
  */
@JSExportAll
case class Callback(override private[amf] val _internal: InternalCallback)
    extends DomainElement
    with NamedAmfObject {

  @JSExportTopLevel("Callback")
  def this() = this(InternalCallback())

  def name: StrField       = _internal.name
  def expression: StrField = _internal.expression
  def endpoint: EndPoint   = _internal.endpoint

  /** Set name property of this Callback. */
  def withName(name: String): this.type = {
    _internal.withName(name)
    this
  }

  /** Set expression property of this Callback. */
  def withExpression(expression: String): this.type = {
    _internal.withExpression(expression)
    this
  }

  /** Set endpoint property of this Callback. */
  def withEndpoint(endpoint: EndPoint): this.type = {
    _internal.withEndpoint(endpoint)
    this
  }

  /** Set endpoint property of this Callback. */
  def withEndpoint(path: String): EndPoint = _internal.withEndpoint(path)
}
