package amf.builder

import amf.model.Organization

/**
  * Created by martin.gutierrez on 6/29/17.
  */
class OrganizationBuilder extends Builder[Organization] {
  var url: String   = _
  var name: String  = _
  var email: String = _

  def withUrl(url: String): OrganizationBuilder = {
    this.url = url
    this
  }

  def withName(name: String): OrganizationBuilder = {
    this.name = name
    this
  }

  def withEmail(email: String): OrganizationBuilder = {
    this.email = email
    this
  }

  override def build: Organization = new Organization(url, name, email)
}

object OrganizationBuilder {
  def apply(): OrganizationBuilder = new OrganizationBuilder()
}