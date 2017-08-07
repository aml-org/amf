package amf.model

import amf.model.builder.WebApiBuilder

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

  val provider: Organization = Organization(webApi.provider)

  val license: License = License(webApi.license)

  val documentation: CreativeWork = CreativeWork(webApi.documentation)

  val baseUriParameters: java.util.List[Parameter] = webApi.baseUriParameters.map(Parameter).asJava

  def toBuilder: WebApiBuilder = WebApiBuilder(webApi.toBuilder)
}
