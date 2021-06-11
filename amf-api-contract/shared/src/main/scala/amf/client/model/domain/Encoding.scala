package amf.client.model.domain
import amf.client.convert.ApiClientConverters._
import amf.core.client.platform.model.domain.DomainElement
import amf.core.client.platform.model.{BoolField, StrField}
import amf.plugins.domain.apicontract.models.{Encoding => InternalEncoding}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * Encoding model class.
  */
@JSExportAll
case class Encoding(override private[amf] val _internal: InternalEncoding) extends DomainElement {

  @JSExportTopLevel("model.domain.Encoding")
  def this() = this(InternalEncoding())

  def propertyName: StrField         = _internal.propertyName
  def contentType: StrField          = _internal.contentType
  def headers: ClientList[Parameter] = _internal.headers.asClient
  def style: StrField                = _internal.style
  def explode: BoolField             = _internal.explode
  def allowReserved: BoolField       = _internal.allowReserved

  /** Set propertyName property of this Encoding. */
  def withPropertyName(propertyName: String): this.type = {
    _internal.withPropertyName(propertyName)
    this
  }

  /** Set contentType property of this Encoding. */
  def withContentType(contentType: String): this.type = {
    _internal.withContentType(contentType)
    this
  }

  /** Set headers property of this Encoding. */
  def withHeaders(headers: ClientList[Parameter]): this.type = {
    _internal.withHeaders(headers.asInternal)
    this
  }

  /** Set style property of this Encoding. */
  def withStyle(style: String): this.type = {
    _internal.withStyle(style)
    this
  }

  /** Set explode property of this Encoding. */
  def withExplode(explode: Boolean): this.type = {
    _internal.withExplode(explode)
    this
  }

  /** Set allowReserved property of this Encoding. */
  def withAllowReserved(allowReserved: Boolean): this.type = {
    _internal.withAllowReserved(allowReserved)
    this
  }

  /**
    * Adds one Parameter to the headers property of this Encoding and returns it for population.
    * Name property of the parameter is required.
    */
  def withHeader(name: String): Parameter = _internal.withHeader(name)
}
