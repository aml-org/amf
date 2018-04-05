package amf.client.model.domain

import amf.client.convert.CoreClientConverters._
import amf.client.model.StrField
import amf.core.model.domain.{ExternalDomainElement => InternalExternalDomainElement}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
@JSExportTopLevel("model.domain.ExternalDomainElement")
case class ExternalDomainElement(private[amf] val _internal: InternalExternalDomainElement) extends DomainElement {

  @JSExportTopLevel("model.domain.ExternalDomainElement")
  def this() = this(InternalExternalDomainElement())

  def raw: StrField       = _internal.raw
  def mediaType: StrField = _internal.mediaType

  def withRaw(raw: String): this.type = {
    _internal.withRaw(raw)
    this
  }

  def withMediaType(mediaType: String): this.type = {
    _internal.withMediaType(mediaType)
    this
  }
}
