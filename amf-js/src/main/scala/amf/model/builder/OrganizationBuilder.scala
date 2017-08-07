package amf.model.builder

import amf.model.Organization
import org.scalajs.dom.experimental.URL

import scala.scalajs.js.annotation.JSExportAll

/**
  *
  */
@JSExportAll
case class OrganizationBuilder(
    private[amf] val internalBuilder: amf.builder.OrganizationBuilder = amf.builder.OrganizationBuilder())
    extends Builder {

  def withUrl(url: URL): OrganizationBuilder = {
    internalBuilder.withUrl(url.href)
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
