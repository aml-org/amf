package amf.domain

import amf.common.AMFAST
import amf.metadata.domain.WebApiModel.{License => WebApiLicense, _}

/**
  * Web Api internal model
  */
case class WebApi(fields: Fields, annotations: Annotations) extends DomainElement {

  def name: String                      = fields(Name)
  def description: String               = fields(Description)
  def host: String                      = fields(Host)
  def schemes: Seq[String]              = fields(Schemes)
  def basePath: String                  = fields(BasePath)
  def accepts: Seq[String]              = fields(Accepts)
  def contentType: Seq[String]          = fields(ContentType)
  def version: String                   = fields(Version)
  def termsOfService: String            = fields(TermsOfService)
  def provider: Organization            = fields(Provider)
  def license: License                  = fields(WebApiLicense)
  def documentation: CreativeWork       = fields(Documentation)
  def endPoints: Seq[EndPoint]          = fields(EndPoints)
  def baseUriParameters: Seq[Parameter] = fields(BaseUriParameters)

  def withName(name: String): this.type                            = set(Name, name)
  def withDescription(description: String): this.type              = set(Description, description)
  def withHost(host: String): this.type                            = set(Host, host)
  def withSchemes(schemes: Seq[String]): this.type                 = set(Schemes, schemes)
  def withEndPoints(endPoints: Seq[EndPoint]): this.type           = setArray(EndPoints, endPoints)
  def withBasePath(path: String): this.type                        = set(BasePath, path)
  def withAccepts(accepts: Seq[String]): this.type                 = set(Accepts, accepts)
  def withContentType(contentType: Seq[String]): this.type         = set(ContentType, contentType)
  def withVersion(version: String): this.type                      = set(Version, version)
  def withTermsOfService(terms: String): this.type                 = set(TermsOfService, terms)
  def withProvider(provider: Organization): this.type              = set(Provider, provider)
  def withLicense(license: License): this.type                     = set(WebApiLicense, license)
  def withDocumentation(documentation: CreativeWork): this.type    = set(Documentation, documentation)
  def withBaseUriParameters(parameters: Seq[Parameter]): this.type = setArray(BaseUriParameters, parameters)

  def withEndPoint(path: String): EndPoint = {
    val result = EndPoint().withPath(path)
    add(EndPoints, result)
    result
  }

  def withBaseUriParameter(name: String): Parameter = {
    val result = Parameter().withName(name)
    add(BaseUriParameters, result)
    result
  }

  override def adopted(parent: String): this.type = withId(parent + "#/web-api")
}

object WebApi {

  def apply(): WebApi = apply(Annotations())

  def apply(ast: AMFAST): WebApi = apply(Annotations(ast))

  def apply(annotations: Annotations): WebApi = new WebApi(Fields(), annotations)
}
