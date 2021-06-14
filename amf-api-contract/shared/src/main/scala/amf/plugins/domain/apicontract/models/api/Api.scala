package amf.plugins.domain.apicontract.models.api

import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.domain.NamedDomainElement
import amf.core.internal.annotations.{SourceVendor, SynthesizedField}
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.ShapeModel.Description
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.core.internal.remote.Vendor
import amf.plugins.domain.shapes.models.{CreativeWork, DocumentedElement}
import amf.plugins.domain.apicontract.metamodel.api.BaseApiModel.{License => WebApiLicense, _}
import amf.plugins.domain.apicontract.models.{
  EndPoint,
  License,
  Organization,
  SecuredElement,
  Server,
  ServerContainer,
  Tag
}
import amf.plugins.domain.apicontract.models.security.SecurityRequirement
import org.yaml.model.{YMap, YNode}

/**
  * Web Api internal model
  */
abstract class Api(fields: Fields, annotations: Annotations)
    extends NamedDomainElement
    with SecuredElement
    with ServerContainer
    with DocumentedElement {

  def description: StrField                      = fields.field(Description)
  def identifier: StrField                       = fields.field(Identifier)
  def schemes: Seq[StrField]                     = fields.field(Schemes)
  def accepts: Seq[StrField]                     = fields.field(Accepts)
  def contentType: Seq[StrField]                 = fields.field(ContentType)
  def version: StrField                          = fields.field(Version)
  def termsOfService: StrField                   = fields.field(TermsOfService)
  def provider: Organization                     = fields.field(Provider)
  def license: License                           = fields.field(WebApiLicense)
  override def documentations: Seq[CreativeWork] = fields.field(Documentations)
  def endPoints: Seq[EndPoint]                   = fields.field(EndPoints)
  def servers: Seq[Server]                       = fields.field(Servers)
  def tags: Seq[Tag]                             = fields(Tags)

  def withDescription(description: String): this.type                  = set(Description, description)
  def withIdentifier(identifier: String): this.type                    = set(Identifier, identifier)
  def withSchemes(schemes: Seq[String]): this.type                     = set(Schemes, schemes)
  def withEndPoints(endPoints: Seq[EndPoint]): this.type               = setArray(EndPoints, endPoints)
  def withAccepts(accepts: Seq[String]): this.type                     = set(Accepts, accepts)
  def withContentType(contentType: Seq[String]): this.type             = set(ContentType, contentType)
  def withVersion(version: String): this.type                          = set(Version, version)
  def withTermsOfService(terms: String): this.type                     = set(TermsOfService, terms)
  def withProvider(provider: Organization): this.type                  = set(Provider, provider)
  def withLicense(license: License): this.type                         = set(WebApiLicense, license)
  def withDocumentations(documentations: Seq[CreativeWork]): this.type = setArray(Documentations, documentations)
  def withServers(servers: Seq[Server]): this.type                     = setArray(Servers, servers)

  def withTags(tags: Seq[Tag]): this.type = setArray(Tags, tags)

  override def removeServers(): Unit = fields.removeField(Servers)

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

  // todo: should source vendor be in the base unit?

  def sourceVendor: Option[Vendor] = annotations.find(classOf[SourceVendor]).map(a => a.vendor)

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = "#/api"

  override def nameField: Field = Name
}
