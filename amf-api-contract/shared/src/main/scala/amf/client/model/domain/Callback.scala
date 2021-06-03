package amf.client.model.domain

import amf.client.model.StrField
import amf.client.convert.ApiClientConverters._

import amf.plugins.domain.apicontract.models.{Callback => InternalCallback}
import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * Callback model class.
  */
@JSExportAll
case class Callback(override private[amf] val _internal: InternalCallback)
    extends DomainElement
    with NamedDomainElement {

  @JSExportTopLevel("model.domain.Callback")
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
