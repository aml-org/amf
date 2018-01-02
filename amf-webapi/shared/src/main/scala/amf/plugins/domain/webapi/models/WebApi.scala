package amf.plugins.domain.webapi.models

import amf.core.annotations.SourceVendor
import amf.core.model.domain.DomainElement
import amf.core.parser.{Annotations, Fields}
import amf.core.remote.Vendor
import amf.plugins.domain.shapes.models.CreativeWork
import amf.plugins.domain.webapi.metamodel.WebApiModel
import amf.plugins.domain.webapi.metamodel.WebApiModel.{License => WebApiLicense, _}
import amf.plugins.domain.webapi.models.security.ParametrizedSecurityScheme
import org.yaml.model.YMap

/**
  * Web Api internal model
  */
case class WebApi(fields: Fields, annotations: Annotations) extends DomainElement {

  def name: String = fields(Name)

  def description: String = fields(Description)

  def host: String = fields(Host)

  def schemes: Seq[String] = fields(Schemes)

  def basePath: String = fields(BasePath)

  def accepts: Seq[String] = fields(Accepts)

  def contentType: Seq[String] = fields(ContentType)

  def version: String = fields(Version)

  def termsOfService: String = fields(TermsOfService)

  def provider: Organization = fields(Provider)

  def license: License = fields(WebApiLicense)

  def documentations: Seq[CreativeWork] = fields(Documentations)

  def endPoints: Seq[EndPoint] = fields(EndPoints)

  def baseUriParameters: Seq[Parameter] = fields(BaseUriParameters)

  def security: Seq[ParametrizedSecurityScheme] = fields(Security)

  def withName(name: String): this.type = set(Name, name)

  def withDescription(description: String): this.type = set(Description, description)

  def withHost(host: String): this.type = set(Host, host)

  def withSchemes(schemes: Seq[String]): this.type = set(Schemes, schemes)

  def withEndPoints(endPoints: Seq[EndPoint]): this.type = setArray(EndPoints, endPoints)

  def withBasePath(path: String): this.type = set(BasePath, path)

  def withAccepts(accepts: Seq[String]): this.type = set(Accepts, accepts)

  def withContentType(contentType: Seq[String]): this.type = set(ContentType, contentType)

  def withVersion(version: String): this.type = set(Version, version)

  def withTermsOfService(terms: String): this.type = set(TermsOfService, terms)

  def withProvider(provider: Organization): this.type = set(Provider, provider)

  def withLicense(license: License): this.type = set(WebApiLicense, license)

  def withDocumentations(documentations: Seq[CreativeWork]): this.type = setArray(Documentations, documentations)

  def withBaseUriParameters(parameters: Seq[Parameter]): this.type = setArray(BaseUriParameters, parameters)

  def withSecurity(security: Seq[ParametrizedSecurityScheme]): this.type = setArray(Security, security)

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

  def withSecurity(name: String): ParametrizedSecurityScheme = {
    val result = ParametrizedSecurityScheme().withName(name)
    add(Security, result)
    result
  }

  def withDocumentationTitle(title: String): CreativeWork = {
    val result = CreativeWork().withTitle(title)
    add(Documentations, result)
    result
  }

  def withDocumentationUrl(url: String): CreativeWork = {
    val result = CreativeWork().withUrl(url)
    add(Documentations, result)
    result
  }

  override def adopted(parent: String): this.type = withId(parent + "#/web-api")

  override def meta = WebApiModel

  // todo: should source vendor be in the base unit?

  def sourceVendor: Option[Vendor] = annotations.find(classOf[SourceVendor]).map(a => a.vendor)
}

object WebApi {

  def apply(): WebApi = apply(Annotations())

  def apply(ast: YMap): WebApi = apply(Annotations(ast))

  def apply(annotations: Annotations): WebApi = WebApi(Fields(), annotations)
}
