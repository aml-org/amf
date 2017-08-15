package amf.domain

import amf.common.AMFAST
import amf.metadata.domain.OrganizationModel.{Email, Name, Url}

/**
  * Organization internal model
  */
case class Organization(fields: Fields, annotations: Annotations) extends DomainElement {

  val url: String   = fields(Url)
  val name: String  = fields(Name)
  val email: String = fields(Email)

  def withUrl(url: String): this.type     = set(Url, url)
  def withName(name: String): this.type   = set(Name, name)
  def withEmail(email: String): this.type = set(Email, email)
}

object Organization {

  def apply(fields: Fields = Fields(), annotations: Annotations = new Annotations()): Organization =
    new Organization(fields, annotations)

  def apply(ast: AMFAST): Organization = apply(Fields(), Annotations(ast))
}
