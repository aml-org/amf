package amf.domain

import amf.builder.WebApiBuilder
import amf.metadata.domain.WebApiModel.{License => WebApiLicense, _}

/**
  * Web Api internal model
  */
case class WebApi(fields: Fields, annotations: List[Annotation]) extends DomainElement {

  override type T = WebApi

  val name: String                      = fields(Name)
  val description: String               = fields(Description)
  val host: String                      = fields(Host)
  val schemes: Seq[String]              = fields(Schemes)
  val basePath: String                  = fields(BasePath)
  val accepts: Seq[String]              = fields(Accepts)
  val contentType: Seq[String]          = fields(ContentType)
  val version: String                   = fields(Version)
  val termsOfService: String            = fields(TermsOfService)
  val provider: Organization            = fields(Provider)
  val license: License                  = fields(WebApiLicense)
  val documentation: CreativeWork       = fields(Documentation)
  val endPoints: Seq[EndPoint]          = fields(EndPoints)
  val baseUriParameters: Seq[Parameter] = fields(BaseUriParameters)

  override def toBuilder: WebApiBuilder = WebApiBuilder(fields, annotations)

  override def id(parent: String): String = parent + "#/web-api"

  override def equals(other: Any): Boolean = other match {
    case that: WebApi =>
      (that canEqual this) &&
        name == that.name &&
        description == that.description &&
        host == that.host &&
        schemes == that.schemes &&
        basePath == that.basePath &&
        accepts == that.accepts &&
        contentType == that.contentType &&
        version == that.version &&
        termsOfService == that.termsOfService &&
        provider == that.provider &&
        license == that.license &&
        documentation == that.documentation &&
        endPoints == that.endPoints &&
        baseUriParameters == that.baseUriParameters
    case _ => false
  }
  def canEqual(other: Any): Boolean = other.isInstanceOf[WebApi]

}
