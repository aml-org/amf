package amf.builder

import amf.maker.BaseUriSplitter
import amf.metadata.domain.APIDocumentationModel
import amf.metadata.domain.APIDocumentationModel._
import amf.domain.{License, _}

/**
  * API Documentation builder
  */
class APIDocumentationBuilder extends Builder[APIDocumentation] {

  def withName(name: String): APIDocumentationBuilder = set(Name, name)

  def withDescription(description: String): APIDocumentationBuilder = set(Description, description)

  def withHost(host: String): APIDocumentationBuilder = set(Host, host)

  def withSchemes(schemes: List[String]): APIDocumentationBuilder = set(Schemes, schemes)

  def withEndPoints(endPoints: List[EndPoint]): APIDocumentationBuilder = set(EndPoints, endPoints)

  def withBasePath(path: String): APIDocumentationBuilder = set(BasePath, path)

  def withAccepts(accepts: String): APIDocumentationBuilder = set(Accepts, accepts)

  def withContentType(contentType: String): APIDocumentationBuilder = set(ContentType, contentType)

  def withVersion(version: String): APIDocumentationBuilder = set(Version, version)

  def withTermsOfService(terms: String): APIDocumentationBuilder = set(TermsOfService, terms)

  def withProvider(provider: Organization): APIDocumentationBuilder = set(Provider, provider)

  def withLicense(license: License): APIDocumentationBuilder = set(APIDocumentationModel.License, license)

  def withDocumentation(documentation: CreativeWork): APIDocumentationBuilder = set(Documentation, documentation)

  protected def fixFields(fields: Fields): Fields = {
    //TODO resolve other builders
    val basePath = Option(fields.get(BasePath))
    val host     = Option(fields.get(Host))
    if (basePath.isEmpty && host.isDefined) {
      fields.set(BasePath, BaseUriSplitter(host.get).path, List())
    }
    fields
  }

  override def build: APIDocumentation = new APIDocumentation(fixFields(fields))
}

object APIDocumentationBuilder {
  def apply(): APIDocumentationBuilder = new APIDocumentationBuilder()

  def apply(fields: Fields): APIDocumentationBuilder = apply().copy(fields)
}
