package amf.model

import amf.model.builder.LicenseBuilder

/**
  * License jvm class
  */
case class License private[model] (private[amf] val license: amf.domain.License) extends DomainElement {

  val url: String = license.url

  val name: String = license.name

  def toBuilder: LicenseBuilder = LicenseBuilder(license.toBuilder)
}
