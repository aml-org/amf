package amf.model

import amf.model.builder.WebApiBuilder

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.JSExportAll

/**
  * WebApi js class
  */
//TODO make construct package private when builder are done

//TODO add javadoc
@JSExportAll
case class WebApi(private val webApi: amf.domain.WebApi) extends DomainElement {

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

  val provider: Organization = amf.model.Organization(webApi.provider)

  val license: License = amf.model.License(webApi.license)

  val documentation: CreativeWork = amf.model.CreativeWork(webApi.documentation)

  val baseUriParameters: js.Iterable[Parameter] = webApi.baseUriParameters.map(amf.model.Parameter).toJSArray

  def toBuilder: WebApiBuilder = WebApiBuilder(webApi.toBuilder)
}
