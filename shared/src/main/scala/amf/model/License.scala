package amf.model

import amf.builder.LicenseBuilder

/**
  * Domain element of type raml-http:License
  *
  * Properties ->
  *     - schema-org:url
  *     - schema-org:name
  */
class License(val url: String, val name: String) extends DomainElement[License, LicenseBuilder] {
  override def toBuilder: LicenseBuilder = ???
}
