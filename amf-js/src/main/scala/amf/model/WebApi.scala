package amf.model

import amf.model.builder.{CreativeWorkBuilder, LicenseBuilder, OrganizationBuilder, WebApiBuilder}

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.JSExportAll

/**
  * WebApi js class
  */
//TODO make construct package private when builder are done

//TODO add javadoc
@JSExportAll
case class WebApi(private[amf] val webApi: amf.domain.WebApi) extends DomainElement {

  val name: String = webApi.name

  val description: String = webApi.description

  val host: String = webApi.host

  val schemes: js.Iterable[String] = webApi.schemes.toJSArray

  val endPoints: js.Iterable[EndPoint] = webApi.endPoints.map(amf.model.EndPoint).toJSArray

  val basePath: String = webApi.basePath

  val accepts: js.Iterable[String] = webApi.accepts.toJSArray

  val contentType: js.Iterable[String] = webApi.contentType.toJSArray

  val version: String = webApi.version

  val termsOfService: String = webApi.termsOfService

  val provider: Organization =
    if (webApi.provider != null) amf.model.Organization(webApi.provider) else OrganizationBuilder().build

  val license: License =
    if (webApi.license != null) amf.model.License(webApi.license) else LicenseBuilder().build

  val documentation: CreativeWork =
    if (webApi.documentation != null) amf.model.CreativeWork(webApi.documentation) else CreativeWorkBuilder().build

  val baseUriParameters: js.Iterable[Parameter] = webApi.baseUriParameters.map(amf.model.Parameter).toJSArray

  def toBuilder: WebApiBuilder = WebApiBuilder(webApi.toBuilder)

  override def equals(other: Any): Boolean = other match {
    case that: WebApi =>
      (that canEqual this) &&
        webApi == that.webApi
    case _ => false
  }

  def canEqual(other: Any): Boolean = other.isInstanceOf[WebApi]
}
