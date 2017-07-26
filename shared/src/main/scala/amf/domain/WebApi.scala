package amf.domain

import amf.builder.WebApiBuilder
import amf.metadata.domain.WebApiModel.{License => WebApiLicense, _}

/**
  * Web Api internal model
  */
case class WebApi(fields: Fields) extends DomainElement {

  override type T = WebApi

  val name: String                = fields(Name)
  val description: String         = fields(Description)
  val host: String                = fields(Host)
  val schemes: Seq[String]        = fields(Schemes)
  val basePath: String            = fields(BasePath)
  val accepts: String             = fields(Accepts)
  val contentType: String         = fields(ContentType)
  val version: String             = fields(Version)
  val termsOfService: String      = fields(TermsOfService)
  val provider: Organization      = fields(Provider)
  val license: License            = fields(WebApiLicense)
  val documentation: CreativeWork = fields(Documentation)
  val endPoints: Seq[EndPoint]    = fields(EndPoints)
  val parameters: Seq[Parameter]  = fields(Parameters)

  override def toBuilder: WebApiBuilder = WebApiBuilder(fields)
}
