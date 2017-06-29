package amf.model
import amf.builder.{Builder, OrganizationBuilder}

/**
  * Domain element of type schema-org:Organization
  *
  * Properties ->
  *     - schema-org:url
  *     - schema-org:name
  *     - schema-org:email
  */
class Organization(val url: String, val name: String, val email: String)
    extends DomainElement[Organization, OrganizationBuilder] {
  override def toBuilder: OrganizationBuilder = OrganizationBuilder().withUrl(url).withName(name).withEmail(email)
}
