package amf.builder

import amf.metadata.model.LicenseModel.{Url, Name}
import amf.model.License

/**
  * License domain element builder.
  */
class LicenseBuilder extends Builder[License] {

  def withUrl(url: String): LicenseBuilder = set(Url, url)

  def withName(name: String): LicenseBuilder = set(Name, name)

  override def build: License = new License(fields)
}

object LicenseBuilder {
  def apply(): LicenseBuilder = new LicenseBuilder()
}
