package amf.model

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.JSExportAll

/**
  * WebApi js class
  */
@JSExportAll
case class WebApi private (private val webApi: amf.domain.WebApi) extends DomainElement {

  def this() = this(amf.domain.WebApi())

  val name: String                     = webApi.name
  val description: String              = webApi.description
  val host: String                     = webApi.host
  val schemes: js.Iterable[String]     = webApi.schemes.toJSArray
  val endPoints: js.Iterable[EndPoint] = webApi.endPoints.map(amf.model.EndPoint).toJSArray
  val basePath: String                 = webApi.basePath
  val accepts: js.Iterable[String]     = webApi.accepts.toJSArray
  val contentType: js.Iterable[String] = webApi.contentType.toJSArray
  val version: String                  = webApi.version
  val termsOfService: String           = webApi.termsOfService
  val provider: Organization =
    if (webApi.provider != null) amf.model.Organization(webApi.provider) else null
  val license: License =
    if (webApi.license != null) amf.model.License(webApi.license) else null
  val documentation: CreativeWork =
    if (webApi.documentation != null) amf.model.CreativeWork(webApi.documentation) else null
  val baseUriParameters: js.Iterable[Parameter] = webApi.baseUriParameters.map(amf.model.Parameter).toJSArray

  override def equals(other: Any): Boolean = other match {
    case that: WebApi =>
      (that canEqual this) &&
        webApi == that.webApi
    case _ => false
  }

  def canEqual(other: Any): Boolean = other.isInstanceOf[WebApi]

  override private[amf] def element: amf.domain.WebApi = webApi

  def withName(name: String): this.type = {
    webApi.withName(name)
    this
  }
  def withDescription(description: String): this.type = {
    webApi.withDescription(description)
    this
  }
  def withHost(host: String): this.type = {
    webApi.withHost(host)
    this
  }
  def withSchemes(schemes: js.Iterable[String]): this.type = {
    webApi.withSchemes(schemes.toSeq)
    this
  }
  def withEndPoints(endPoints: js.Iterable[EndPoint]): this.type = {
    webApi.withEndPoints(endPoints.toSeq.map(_.element))
    this
  }
  def withBasePath(path: String): this.type = {
    webApi.withBasePath(path)
    this
  }
  def withAccepts(accepts: js.Iterable[String]): this.type = {
    webApi.withAccepts(accepts.toSeq)
    this
  }
  def withContentType(contentType: js.Iterable[String]): this.type = {
    webApi.withContentType(contentType.toSeq)
    this
  }
  def withVersion(version: String): this.type = {
    webApi.withVersion(version)
    this
  }
  def withTermsOfService(terms: String): this.type = {
    webApi.withTermsOfService(terms)
    this
  }
  def withProvider(provider: Organization): this.type = {
    webApi.withProvider(provider.element)
    this
  }
  def withLicense(license: License): this.type = {
    webApi.withLicense(license.element)
    this
  }
  def withDocumentation(documentation: CreativeWork): this.type = {
    webApi.withDocumentation(documentation.element)
    this
  }
  def withBaseUriParameters(parameters: js.Iterable[Parameter]): this.type = {
    webApi.withBaseUriParameters(parameters.toSeq.map(_.element))
    this
  }

  def withEndPoint(path: String): EndPoint = EndPoint(webApi.withEndPoint(path))

  def withBaseUriParameter(name: String): Parameter = Parameter(webApi.withBaseUriParameter(name))
}
