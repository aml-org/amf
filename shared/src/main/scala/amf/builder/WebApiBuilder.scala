package amf.builder

import amf.domain.{License, _}
import amf.maker.BaseUriSplitter
import amf.metadata.domain.WebApiModel
import amf.metadata.domain.WebApiModel._

/**
  * Web Api builder
  */
class WebApiBuilder extends Builder {

  override type T = WebApi

  def withName(name: String): WebApiBuilder = set(Name, name)

  def withDescription(description: String): WebApiBuilder = set(Description, description)

  def withHost(host: String): WebApiBuilder = set(Host, host)

  def withSchemes(schemes: List[String]): WebApiBuilder = set(Schemes, schemes)

  def withEndPoints(endPoints: List[EndPoint]): WebApiBuilder = set(EndPoints, endPoints)

  def withBasePath(path: String): WebApiBuilder = set(BasePath, path)

  def withAccepts(accepts: List[String]): WebApiBuilder = set(Accepts, accepts)

  def withContentType(contentType: List[String]): WebApiBuilder = set(ContentType, contentType)

  def withVersion(version: String): WebApiBuilder = set(Version, version)

  def withTermsOfService(terms: String): WebApiBuilder = set(TermsOfService, terms)

  def withProvider(provider: Organization): WebApiBuilder = set(Provider, provider)

  def withLicense(license: License): WebApiBuilder = set(WebApiModel.License, license)

  def withDocumentation(documentation: CreativeWork): WebApiBuilder = set(Documentation, documentation)

  def withBaseUriParameters(parameters: Seq[Parameter]): WebApiBuilder = set(BaseUriParameters, parameters)

  protected def fixFields(fields: Fields): Fields = {
    val basePath = Option(fields(BasePath))
    val host     = Option(fields(Host))
    if (basePath.isEmpty && host.isDefined) {
      fields.set(BasePath, BaseUriSplitter(host.get).path, List())
    }
    fields
  }

  override def build: WebApi = WebApi(fixFields(fields), annotations)
}

object WebApiBuilder {
  def apply(): WebApiBuilder = apply(Nil)

  def apply(fields: Fields, annotations: List[Annotation] = Nil): WebApiBuilder = apply(annotations).copy(fields)

  def apply(annotations: List[Annotation]): WebApiBuilder = new WebApiBuilder().withAnnotations(annotations)
}
