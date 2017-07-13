package amf.builder

import amf.metadata.domain.OrganizationModel.{Url, Name, Email}
import amf.model.Organization

/**
  * Created by martin.gutierrez on 6/29/17.
  */
class OrganizationBuilder extends Builder[Organization] {

  def withUrl(url: String): OrganizationBuilder = set(Url, url)

  def withName(name: String): OrganizationBuilder = set(Name, name)

  def withEmail(email: String): OrganizationBuilder = set(Email, email)

  override def build: Organization = Organization(fields)
}

object OrganizationBuilder {
  def apply(): OrganizationBuilder = new OrganizationBuilder()
}
