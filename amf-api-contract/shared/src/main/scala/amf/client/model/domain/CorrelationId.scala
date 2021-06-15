package amf.client.model.domain

import amf.core.client.platform.model.StrField
import amf.client.convert.ApiClientConverters._
import amf.core.client.platform.model.domain.{DomainElement, Linkable, NamedDomainElement}

import amf.plugins.domain.apicontract.models.{CorrelationId => InternalCorrelationId}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * CorrelationId model class.
  */
@JSExportAll
case class CorrelationId(override private[amf] val _internal: InternalCorrelationId)
    extends DomainElement
    with Linkable
    with NamedDomainElement {

  @JSExportTopLevel("model.domain.CorrelationId")
  def this() = this(InternalCorrelationId())

  def description: StrField = _internal.description
  def idLocation: StrField  = _internal.idLocation

  def withDescription(description: String): this.type = {
    _internal.withDescription(description)
    this
  }
  def withIdLocation(idLocation: String): this.type = {
    _internal.withIdLocation(idLocation)
    this
  }
  override def linkCopy(): CorrelationId = _internal.linkCopy()

  override def name: StrField = _internal.name

  override def withName(name: String): this.type = {
    _internal.withName(name)
    this
  }
}
