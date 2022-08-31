package amf.apicontract.client.platform.model.domain

import amf.core.client.platform.model.StrField
import amf.core.client.platform.model.domain.{DomainElement, NamedDomainElement}
import amf.apicontract.client.scala.model.domain.{Organization => InternalOrganization}
import amf.apicontract.internal.convert.ApiClientConverters._

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/** Organization model class.
  */
@JSExportAll
case class Organization(override private[amf] val _internal: InternalOrganization)
    extends DomainElement
    with NamedDomainElement {

  @JSExportTopLevel("Organization")
  def this() = this(InternalOrganization())

  def url: StrField   = _internal.url
  def name: StrField  = _internal.name
  def email: StrField = _internal.email

  /** Set url property of this Organization. */
  def withUrl(url: String): this.type = {
    _internal.withUrl(url)
    this
  }

  /** Set name property of this Organization */
  def withName(name: String): this.type = {
    _internal.withName(name)
    this
  }

  /** Set email property of this Organization */
  def withEmail(email: String): this.type = {
    _internal.withEmail(email)
    this
  }
}
