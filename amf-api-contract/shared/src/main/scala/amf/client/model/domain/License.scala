package amf.client.model.domain
import amf.client.convert.ApiClientConverters._

import amf.client.model.StrField
import amf.plugins.domain.apicontract.models.{License => InternalLicense}
import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * License model class.
  */
@JSExportAll
case class License(override private[amf] val _internal: InternalLicense)
    extends DomainElement
    with NamedDomainElement {

  @JSExportTopLevel("model.domain.License")
  def this() = this(InternalLicense())

  def url: StrField  = _internal.url
  def name: StrField = _internal.name

  /** Set url property of this License. */
  def withUrl(url: String): this.type = {
    _internal.withUrl(url)
    this
  }

  /** Set name property of this License. */
  def withName(name: String): this.type = {
    _internal.withName(name)
    this
  }
}
