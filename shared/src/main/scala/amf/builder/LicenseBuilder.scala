package amf.builder

import amf.model.License

/**
  * License domain element builder.
  */
class LicenseBuilder extends Builder[License] {
  var url: String  = _
  var name: String = _

  def withUrl(url: String): LicenseBuilder = {
    this.url = url
    this
  }

  def withName(name: String): LicenseBuilder = {
    this.name = name
    this
  }

  override def build: License = new License(url, name)
}

object LicenseBuilder {
  def apply(): LicenseBuilder = new LicenseBuilder()
}
