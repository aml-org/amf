package amf.builder

import amf.metadata.domain.LicenseModel.{Name, Url}
import amf.domain.{Annotation, Fields, License}

/**
  * License domain element builder.
  */
class LicenseBuilder extends Builder {

  override type T = License

  def withUrl(url: String): LicenseBuilder = set(Url, url)

  def withName(name: String): LicenseBuilder = set(Name, name)

  override def build: License = License(fields)
}

object LicenseBuilder {
  def apply(): LicenseBuilder = new LicenseBuilder()

  def apply(annotations: List[Annotation]): LicenseBuilder = new LicenseBuilder()

  def apply(fields: Fields): LicenseBuilder = apply().copy(fields)
}
