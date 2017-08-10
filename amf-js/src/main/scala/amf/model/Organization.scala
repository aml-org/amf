package amf.model

import amf.model.builder.OrganizationBuilder

import scala.scalajs.js.annotation.JSExportAll

/**
  * Organization js class
  */
@JSExportAll
case class Organization private[model] (private[amf] val organization: amf.domain.Organization) extends DomainElement {

  val url: String = organization.url

  val name: String = organization.name

  val email: String = organization.email

  def toBuilder: OrganizationBuilder = OrganizationBuilder(organization.toBuilder)

  override def equals(other: Any): Boolean = other match {
    case that: Organization =>
      (that canEqual this) &&
        organization == that.organization
    case _ => false
  }

  def canEqual(other: Any): Boolean = other.isInstanceOf[Organization]
}
