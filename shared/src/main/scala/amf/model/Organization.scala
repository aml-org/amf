package amf.model
import amf.builder.OrganizationBuilder

import scala.scalajs.js.annotation.JSExportAll

/**
  * Domain element of type schema-org:Organization
  *
  * Properties ->
  *     - schema-org:url
  *     - schema-org:name
  *     - schema-org:email
  */
@JSExportAll
case class Organization(url: String, name: String, email: String)
    extends DomainElement[Organization, OrganizationBuilder] {
  override def toBuilder: OrganizationBuilder = OrganizationBuilder().withUrl(url).withName(name).withEmail(email)
}
