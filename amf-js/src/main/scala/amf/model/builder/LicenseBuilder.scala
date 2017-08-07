package amf.model.builder

import amf.model.License
import org.scalajs.dom.experimental.URL

import scala.scalajs.js.annotation.JSExportAll

/**
  * License domain element builder.
  */
@JSExportAll
case class LicenseBuilder(private[amf] val licenseBuilder: amf.builder.LicenseBuilder = amf.builder.LicenseBuilder())
    extends Builder {

  def withUrl(url: URL): LicenseBuilder = {
    licenseBuilder.withUrl(url.href)
    this
  }

  def withName(name: String): LicenseBuilder = {
    licenseBuilder.withName(name)
    this
  }

  def build: License = License(licenseBuilder.build)
}
