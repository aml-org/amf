package amf.builder

import amf.maker.BaseUriSplitter
import amf.metadata.model.WebApiModel
import amf.metadata.model.WebApiModel._
import amf.model.{BaseWebApi, CreativeWork, License, Organization, EndPoint, Fields}

import scala.scalajs.js.annotation.JSExportAll

/**
  * Created by martin.gutierrez on 7/3/17.
  */
@JSExportAll
trait BaseWebApiBuilder extends Builder[BaseWebApi] {

  def withName(name: String): this.type = set(Name, name)

  def withDescription(description: String): this.type = set(Description, description)

  def withHost(host: String): this.type = set(Host, host)

  def withSchemes(schemes: List[String]): this.type = set(Schemes, schemes)

  def withEndPoints(endPoints: List[EndPoint]): this.type = set(EndPoints, endPoints)

  def withBasePath(path: String): this.type = set(BasePath, path)

  def withAccepts(accepts: String): this.type = set(Accepts, accepts)

  def withContentType(contentType: String): this.type = set(ContentType, contentType)

  def withVersion(version: String): this.type = set(Version, version)

  def withTermsOfService(terms: String): this.type = set(TermsOfService, terms)

  def withProvider(provider: Organization): this.type = set(Provider, provider)

  def withLicense(license: License): this.type = set(License, license)

  def withDocumentation(documentation: CreativeWork): this.type = set(Documentation, documentation)

  protected def fixFields(fields: Fields): Fields = {
    //TODO resolve other builders
    val basePath = Option(fields.get(WebApiModel.BasePath))
    val host     = Option(fields.get(WebApiModel.Host))
    if (basePath.isEmpty && host.isDefined) {
      fields.set(WebApiModel.BasePath, BaseUriSplitter(host.get).path, List())
    }
    fields
  }
}
