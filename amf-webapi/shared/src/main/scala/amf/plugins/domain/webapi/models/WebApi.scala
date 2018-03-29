package amf.plugins.domain.webapi.models

import amf.client.model.StrField
import amf.core.annotations.{SourceVendor, SynthesizedField}
import amf.core.metamodel.Obj
import amf.core.model.domain.DomainElement
import amf.core.parser.{Annotations, Fields}
import amf.core.remote.Vendor
import amf.plugins.domain.shapes.models.CreativeWork
import amf.plugins.domain.webapi.metamodel.WebApiModel
import amf.plugins.domain.webapi.metamodel.WebApiModel.{License => WebApiLicense, _}
import amf.plugins.domain.webapi.models.security.ParametrizedSecurityScheme
import org.yaml.model.YMap

/**
  * Web Api internal model
  */
case class WebApi(fields: Fields, annotations: Annotations) extends DomainElement {

  def name: StrField                            = fields.field(Name)
  def description: StrField                     = fields.field(Description)
  def schemes: Seq[StrField]                    = fields.field(Schemes)
  def accepts: Seq[StrField]                    = fields.field(Accepts)
  def contentType: Seq[StrField]                = fields.field(ContentType)
  def version: StrField                         = fields.field(Version)
  def termsOfService: StrField                  = fields.field(TermsOfService)
  def provider: Organization                    = fields.field(Provider)
  def license: License                          = fields.field(WebApiLicense)
  def documentations: Seq[CreativeWork]         = fields.field(Documentations)
  def endPoints: Seq[EndPoint]                  = fields.field(EndPoints)
  def servers: Seq[Server]                      = fields.field(Servers)
  def security: Seq[ParametrizedSecurityScheme] = fields.field(Security)
  def tags: Seq[Tag]                            = fields(Tags)

  def withName(name: String): this.type                                  = set(Name, name)
  def withDescription(description: String): this.type                    = set(Description, description)
  def withSchemes(schemes: Seq[String]): this.type                       = set(Schemes, schemes)
  def withEndPoints(endPoints: Seq[EndPoint]): this.type                 = setArray(EndPoints, endPoints)
  def withAccepts(accepts: Seq[String]): this.type                       = set(Accepts, accepts)
  def withContentType(contentType: Seq[String]): this.type               = set(ContentType, contentType)
  def withVersion(version: String): this.type                            = set(Version, version)
  def withTermsOfService(terms: String): this.type                       = set(TermsOfService, terms)
  def withProvider(provider: Organization): this.type                    = set(Provider, provider)
  def withLicense(license: License): this.type                           = set(WebApiLicense, license)
  def withDocumentations(documentations: Seq[CreativeWork]): this.type   = setArray(Documentations, documentations)
  def withServers(servers: Seq[Server]): this.type                       = setArray(Servers, servers)
  def withSecurity(security: Seq[ParametrizedSecurityScheme]): this.type = setArray(Security, security)

  def withTags(tags: Seq[Tag]): this.type = setArray(Tags, tags)

  def withEndPoint(path: String): EndPoint = {
    val result = EndPoint().withPath(path)
    add(EndPoints, result)
    result
  }

  def withDefaultServer(url: String): Server = withServer(url).add(SynthesizedField())

  def withServer(url: String): Server = {
    val result = Server().withUrl(url)
    add(Servers, result)
    result
  }

  def withSecurity(name: String): ParametrizedSecurityScheme = {
    val result = ParametrizedSecurityScheme().withName(name)
    add(Security, result)
    result
  }

  def withDocumentationTitle(title: String): CreativeWork = {
    val result = CreativeWork().withTitle(title)
    add(Documentations, result)
    result
  }

  def withDocumentationUrl(url: String): CreativeWork = {
    val result = CreativeWork().withUrl(url)
    add(Documentations, result)
    result
  }

  override def meta: Obj = WebApiModel

  // todo: should source vendor be in the base unit?

  def sourceVendor: Option[Vendor] = annotations.find(classOf[SourceVendor]).map(a => a.vendor)

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = "#/web-api"
}

object WebApi {

  def apply(): WebApi = apply(Annotations())

  def apply(ast: YMap): WebApi = apply(Annotations(ast))

  def apply(annotations: Annotations): WebApi = WebApi(Fields(), annotations)
}
