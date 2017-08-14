package amf.model.builder

import java.net.URL

import amf.model.Organization

/**
  *
  */
case class OrganizationBuilder private (
    private val internalBuilder: amf.builder.OrganizationBuilder = amf.builder.OrganizationBuilder())
    extends Builder {

  def this() = this(amf.builder.OrganizationBuilder())

  def withUrl(url: URL): OrganizationBuilder = {
    internalBuilder.withUrl(url.getRef)
    this
  }

  def withName(name: String): OrganizationBuilder = {
    internalBuilder.withName(name)
    this
  }

  def withEmail(email: String): OrganizationBuilder = {
    internalBuilder.withEmail(email)
    this
  }

  def build: Organization = Organization(internalBuilder.build)
}
