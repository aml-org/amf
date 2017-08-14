package amf.model.builder

import java.net.URL

import amf.model.License

/**
  * License domain element builder.
  */
case class LicenseBuilder private (
    private val licenseBuilder: amf.builder.LicenseBuilder = amf.builder.LicenseBuilder())
    extends Builder {

  def this() = this(amf.builder.LicenseBuilder())

  def withUrl(url: URL): LicenseBuilder = {
    licenseBuilder.withUrl(url.getRef)
    this
  }

  def withName(name: String): LicenseBuilder = {
    licenseBuilder.withName(name)
    this
  }

  def build: License = License(licenseBuilder.build)
}
