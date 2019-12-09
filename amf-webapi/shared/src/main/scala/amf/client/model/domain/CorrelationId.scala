package amf.client.model.domain

import amf.client.model.StrField
import amf.client.convert.WebApiClientConverters._
import amf.plugins.domain.webapi.models.{CorrelationId => InternalCorrelationId}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * CorrelationId model class.
  */
@JSExportAll
class CorrelationId(override private[amf] val _internal: InternalCorrelationId) extends DomainElement {

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
}
