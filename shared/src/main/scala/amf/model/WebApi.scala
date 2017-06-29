package amf.model

import amf.builder.WebApiBuilder

/**
  * Domain model of type schema-org:WebApi
  *
  * Properties ->
  *     - schema-org:name
  *     - schema-org:description
  *     - raml-http:host
  *     - raml-http:scheme
  *     - raml-http:basePath
  *     - raml-http:accepts
  *     - raml-http:contentType
  *     - schema-org:version
  *     - schema-org:termsOfService
  *     - schema-org:provider => instance of type schema-org:Organization
  *     - schema-org:license => instance of type raml-http:License
  *     - schema-org:documentation => instance of type schema-org:CreativeWork
  */
class WebApi(val name: String,
             val description: String,
             val host: String,
             val scheme: List[String],
             val basePath: String,
             val accepts: String,
             val contentType: String,
             val version: String,
             val termsOfService: String,
             val provider: Organization,
             val license: License,
             val documentation: CreativeWork)
    extends DomainElement[WebApi, WebApiBuilder]
    with RootDomainElement {

  override def toBuilder: WebApiBuilder = ???
}
