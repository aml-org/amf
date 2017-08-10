package amf.model

import amf.model.builder.{CreativeWorkBuilder, LicenseBuilder, OrganizationBuilder, WebApiBuilder}

import scala.collection.JavaConverters._

/**
  * WebApi java class
  */
//TODO make construct package private when builders are done

//TODO add javadoc
case class WebApi(private val webApi: amf.domain.WebApi) extends DomainElement {

  val name: String = webApi.name

  val description: String = webApi.description

  val host: String = webApi.host

  val schemes: java.util.List[String] = webApi.schemes.asJava

  val endPoints: java.util.List[EndPoint] = webApi.endPoints.map(EndPoint).asJava

  val basePath: String = webApi.basePath

  val accepts: java.util.List[String] = webApi.accepts.asJava

  val contentType: java.util.List[String] = webApi.contentType.asJava

  val version: String = webApi.version

  val termsOfService: String = webApi.termsOfService

  val provider: Organization =
    if (webApi.provider != null) Organization(webApi.provider) else OrganizationBuilder().build

  val license: License =
    if (webApi.license != null) License(webApi.license) else LicenseBuilder().build

  val documentation: CreativeWork =
    if (webApi.documentation != null) CreativeWork(webApi.documentation) else CreativeWorkBuilder().build

  val baseUriParameters: java.util.List[Parameter] = webApi.baseUriParameters.map(Parameter).asJava

  def toBuilder: WebApiBuilder = WebApiBuilder(webApi.toBuilder)

  override def equals(other: Any): Boolean = other match {
    case that: WebApi =>
      (that canEqual this) &&
        webApi == that.webApi
    case _ => false
  }

  def canEqual(other: Any): Boolean = other.isInstanceOf[WebApi]
}
