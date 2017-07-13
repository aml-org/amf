package amf.model
import amf.builder.OrganizationBuilder
import amf.metadata.domain.OrganizationModel.{Email, Name, Url}

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
case class Organization(fields: Fields) extends DomainElement[Organization, OrganizationBuilder] {
  override def toBuilder: OrganizationBuilder = OrganizationBuilder().withUrl(url).withName(name).withEmail(email)

  val url: String   = fields get Url
  val name: String  = fields get Name
  val email: String = fields get Email

  def canEqual(other: Any): Boolean = other.isInstanceOf[Organization]

  override def equals(other: Any): Boolean = other match {
    case that: Organization =>
      (that canEqual this) &&
        url == that.url &&
        name == that.name &&
        email == that.email
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(url, name, email)
    state.map(p => if (p != null) p.hashCode() else 0).foldLeft(0)((a, b) => 31 * a + b)
  }

  override def toString = s"Organization($url, $name, $email)"

}
