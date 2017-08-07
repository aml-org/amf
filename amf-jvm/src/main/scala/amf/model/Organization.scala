package amf.model

import amf.model.builder.OrganizationBuilder

/**
  * Organziation jvm class
  */
case class Organization private[model] (private[amf] val organization: amf.domain.Organization) extends DomainElement {

  val url: String = organization.url

  val name: String = organization.name

  val email: String = organization.email

  def toBuilder: OrganizationBuilder = OrganizationBuilder(organization.toBuilder)
}
