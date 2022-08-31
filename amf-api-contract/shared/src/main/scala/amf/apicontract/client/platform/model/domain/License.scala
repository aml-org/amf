package amf.apicontract.client.platform.model.domain

import amf.core.client.platform.model.StrField
import amf.core.client.platform.model.domain.{DomainElement, NamedAmfObject}
import amf.apicontract.client.scala.model.domain.{License => InternalLicense}
import amf.apicontract.internal.convert.ApiClientConverters._

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/** License model class.
  */
@JSExportAll
case class License(override private[amf] val _internal: InternalLicense) extends DomainElement with NamedAmfObject {

  @JSExportTopLevel("License")
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
