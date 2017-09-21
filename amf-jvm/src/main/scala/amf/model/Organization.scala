package amf.model

/**
  * JVM Organization model class
  */
case class Organization private[model] (private val organization: amf.domain.Organization) extends DomainElement {

  def this() = this(amf.domain.Organization())

  val url: String   = organization.url
  val name: String  = organization.name
  val email: String = organization.email

  override private[amf] def element: amf.domain.Organization = organization

  /** Set url property of this [[Organization]]. */
  def withUrl(url: String): this.type = {
    organization.withUrl(url)
    this
  }

  /** Set name property of this [[Organization]]. */
  def withName(name: String): this.type = {
    organization.withName(name)
    this
  }

  /** Set email property of this [[Organization]]. */
  def withEmail(email: String): this.type = {
    organization.withEmail(email)
    this
  }
}
