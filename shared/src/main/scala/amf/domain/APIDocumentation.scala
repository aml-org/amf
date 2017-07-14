package amf.domain

import amf.builder.APIDocumentationBuilder
import amf.metadata.domain.APIDocumentationModel
import amf.metadata.domain.APIDocumentationModel._

/**
  * API documentation internal model
  */
class APIDocumentation(val fields: Fields) extends DomainElement {

  override type This = APIDocumentation

  val name: String                = fields get Name
  val description: String         = fields get Description
  val host: String                = fields get Host
  val schemes: Seq[String]        = fields get Schemes
  val basePath: String            = fields get BasePath
  val accepts: String             = fields get Accepts
  val contentType: String         = fields get ContentType
  val version: String             = fields get Version
  val termsOfService: String      = fields get TermsOfService
  val provider: Organization      = fields get Provider
  val license: License            = fields get APIDocumentationModel.License
  val documentation: CreativeWork = fields get Documentation
  val endPoints: Seq[EndPoint]    = fields get EndPoints

  override def toBuilder: APIDocumentationBuilder = APIDocumentationBuilder(fields)
}
