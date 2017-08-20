package amf.domain

import amf.common.AMFAST
import amf.metadata.domain.OrganizationModel.{Email, Name, Url}

/**
  * Organization internal model
  */
case class Organization(fields: Fields, annotations: Annotations) extends DomainElement {

  def url: String   = fields(Url)
  def name: String  = fields(Name)
  def email: String = fields(Email)

  def withUrl(url: String): this.type     = set(Url, url)
  def withName(name: String): this.type   = set(Name, name)
  def withEmail(email: String): this.type = set(Email, email)

  override def adopted(parent: String): this.type = withId(parent + "/organization")
}

object Organization {

  def apply(): Organization = apply(Annotations())

  def apply(ast: AMFAST): Organization = apply(Annotations(ast))

  def apply(annotations: Annotations): Organization = apply(Fields(), annotations)
}
