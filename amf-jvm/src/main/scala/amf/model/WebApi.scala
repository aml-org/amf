package amf.model

import scala.collection.JavaConverters._

/**
  * WebApi java class
  */
case class WebApi private (private val webApi: amf.domain.WebApi) extends DomainElement {

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
}
