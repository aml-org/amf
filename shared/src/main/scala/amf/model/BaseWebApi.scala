package amf.model

import amf.builder.BaseWebApiBuilder
import amf.metadata.model.WebApiModel._

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
abstract class BaseWebApi(private val fields: Fields)
    extends ApiDocumentation[BaseWebApi, BaseWebApiBuilder]
    with RootDomainElement {

  val name: String                = fields get Name
  val description: String         = fields get Description
  val host: String                = fields get Host
  val schemes: List[String]       = fields get Schemes
  val basePath: String            = fields get BasePath
  val accepts: String             = fields get Accepts
  val contentType: String         = fields get ContentType
  val version: String             = fields get Version
  val termsOfService: String      = fields get TermsOfService
  val provider: Organization      = fields get Provider
  val license: License            = fields get License
  val documentation: CreativeWork = fields get Documentation

}
