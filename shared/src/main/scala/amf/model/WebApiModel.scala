package amf.model

import amf.builder.BaseWebApiBuilder

import scala.scalajs.js.annotation.JSExportAll

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
@JSExportAll
trait WebApiModel extends ApiDocumentation[WebApiModel, BaseWebApiBuilder] with RootDomainElement {

  val name: String
  val description: String
  val host: String
  protected val schemeList: List[String]
  val basePath: String
  val accepts: String
  val contentType: String
  val version: String
  val termsOfService: String
  val provider: Organization
  val license: License
  val documentation: CreativeWork

  protected def createBuilder(): BaseWebApiBuilder

  override def toBuilder: BaseWebApiBuilder =
    createBuilder()
      .withName(name)
      .withDescription(description)
      .withHost(host)
      .withScheme(schemeList)
      .withBasePath(basePath)
      .withAccepts(accepts)
      .withContentType(contentType)
      .withVersion(version)
      .withTermsOfService(termsOfService)
      .withProvider(provider)
      .withLicense(license)
      .withDocumentation(documentation)
}
