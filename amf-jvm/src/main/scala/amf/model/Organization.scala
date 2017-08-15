package amf.model

/**
  * Organziation jvm class
  */
case class Organization private[model] (private val organization: amf.domain.Organization) extends DomainElement {

  val url: String   = organization.url
  val name: String  = organization.name
  val email: String = organization.email

  override def equals(other: Any): Boolean = other match {
    case that: Organization =>
      (that canEqual this) &&
        organization == that.organization
    case _ => false
  }

  def canEqual(other: Any): Boolean = other.isInstanceOf[Organization]

  override private[amf] def element: amf.domain.Organization = organization
}
