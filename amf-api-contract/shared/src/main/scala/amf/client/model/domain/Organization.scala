package amf.client.model.domain
import amf.client.convert.ApiClientConverters._

import amf.core.client.platform.model.StrField
import amf.plugins.domain.apicontract.models.{Organization => InternalOrganization}
import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}
import amf.core.client.platform.model.domain.{DomainElement, Linkable, NamedDomainElement}

/**
  * Organization model class.
  */
@JSExportAll
case class Organization(override private[amf] val _internal: InternalOrganization)
    extends DomainElement
    with NamedDomainElement {

  @JSExportTopLevel("model.domain.Organization")
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
