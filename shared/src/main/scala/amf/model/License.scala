package amf.model

import amf.builder.LicenseBuilder

import scala.scalajs.js.annotation.JSExportAll

/**
  * Domain element of type raml-http:License
  *
  * Properties ->
  *     - schema-org:url
  *     - schema-org:name
  */
@JSExportAll
class License(val url: String, val name: String) extends DomainElement[License, LicenseBuilder] {
  override def toBuilder: LicenseBuilder = LicenseBuilder().withUrl(url).withName(name)
}
