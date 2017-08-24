package amf.model

import scala.collection.JavaConverters._

/**
  * WebApi java class
  */
case class WebApi private (private val webApi: amf.domain.WebApi) extends DomainElement {

  def this() = this(amf.domain.WebApi())

  val name: String                        = webApi.name
  val description: String                 = webApi.description
  val host: String                        = webApi.host
  val schemes: java.util.List[String]     = webApi.schemes.asJava
  val endPoints: java.util.List[EndPoint] = webApi.endPoints.map(EndPoint).asJava
  val basePath: String                    = webApi.basePath
  val accepts: java.util.List[String]     = webApi.accepts.asJava
  val contentType: java.util.List[String] = webApi.contentType.asJava
  val version: String                     = webApi.version
  val termsOfService: String              = webApi.termsOfService
  val provider: Organization =
    if (webApi.provider != null) Organization(webApi.provider) else null
  val license: License =
    if (webApi.license != null) License(webApi.license) else null
  val documentation: CreativeWork =
    if (webApi.documentation != null) CreativeWork(webApi.documentation) else null
  val baseUriParameters: java.util.List[Parameter] = webApi.baseUriParameters.map(Parameter).asJava

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
  def withSchemes(schemes: java.util.List[String]): this.type = {
    webApi.withSchemes(schemes.asScala)
    this
  }
  def withEndPoints(endPoints: java.util.List[EndPoint]): this.type = {
    webApi.withEndPoints(endPoints.asScala.map(_.element))
    this
  }
  def withBasePath(path: String): this.type = {
    webApi.withBasePath(path)
    this
  }
  def withAccepts(accepts: java.util.List[String]): this.type = {
    webApi.withAccepts(accepts.asScala)
    this
  }
  def withContentType(contentType: java.util.List[String]): this.type = {
    webApi.withContentType(contentType.asScala)
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
  def withBaseUriParameters(parameters: java.util.List[Parameter]): this.type = {
    webApi.withBaseUriParameters(parameters.asScala.map(_.element))
    this
  }

  def withEndPoint(path: String): EndPoint = EndPoint(webApi.withEndPoint(path))

  def withBaseUriParameter(name: String): Parameter = Parameter(webApi.withBaseUriParameter(name))
}
