package amf.domain

import amf.common.AMFAST
import amf.metadata.domain.WebApiModel.{License => WebApiLicense, _}

/**
  * Web Api internal model
  */
case class WebApi(fields: Fields, annotations: Annotations) extends DomainElement {

  val name: String                      = fields(Name)
  val description: String               = fields(Description)
  val host: String                      = fields(Host)
  val schemes: Seq[String]              = fields(Schemes)
  val basePath: String                  = fields(BasePath)
  val accepts: Seq[String]              = fields(Accepts)
  val contentType: Seq[String]          = fields(ContentType)
  val version: String                   = fields(Version)
  val termsOfService: String            = fields(TermsOfService)
  val provider: Organization            = fields(Provider)
  val license: License                  = fields(WebApiLicense)
  val documentation: CreativeWork       = fields(Documentation)
  val endPoints: Seq[EndPoint]          = fields(EndPoints)
  val baseUriParameters: Seq[Parameter] = fields(BaseUriParameters)

  def withName(name: String): this.type                            = set(Name, name)
  def withDescription(description: String): this.type              = set(Description, description)
  def withHost(host: String): this.type                            = set(Host, host)
  def withSchemes(schemes: Seq[String]): this.type                 = set(Schemes, schemes)
  def withEndPoints(endPoints: Seq[EndPoint]): this.type           = set(EndPoints, endPoints)
  def withBasePath(path: String): this.type                        = set(BasePath, path)
  def withAccepts(accepts: Seq[String]): this.type                 = set(Accepts, accepts)
  def withContentType(contentType: Seq[String]): this.type         = set(ContentType, contentType)
  def withVersion(version: String): this.type                      = set(Version, version)
  def withTermsOfService(terms: String): this.type                 = set(TermsOfService, terms)
  def withProvider(provider: Organization): this.type              = set(Provider, provider)
  def withLicense(license: License): this.type                     = set(WebApiLicense, license)
  def withDocumentation(documentation: CreativeWork): this.type    = set(Documentation, documentation)
  def withBaseUriParameters(parameters: Seq[Parameter]): this.type = set(BaseUriParameters, parameters)
}

object WebApi {

  def apply(fields: Fields = Fields(), annotations: Annotations = new Annotations()): WebApi =
    new WebApi(fields, annotations)

  def apply(ast: AMFAST): WebApi = new WebApi(fields, annotations)
}
