package amf.builder

import amf.metadata.domain.OrganizationModel.{Email, Name, Url}
import amf.domain.{Annotation, Fields, Organization}

/**
  *
  */
class OrganizationBuilder extends Builder {

  override type T = Organization

  def withUrl(url: String): OrganizationBuilder = set(Url, url)

  def withName(name: String): OrganizationBuilder = set(Name, name)

  def withEmail(email: String): OrganizationBuilder = set(Email, email)

  override def resolveId(container: String): this.type = withId(container + "/organization")

  override def build: Organization = Organization(fields, annotations)
}

object OrganizationBuilder {
  def apply(): OrganizationBuilder = apply(Nil)

  def apply(fields: Fields, annotations: List[Annotation] = Nil): OrganizationBuilder = apply(annotations).copy(fields)

  def apply(annotations: List[Annotation]): OrganizationBuilder =
    new OrganizationBuilder().withAnnotations(annotations)
}
