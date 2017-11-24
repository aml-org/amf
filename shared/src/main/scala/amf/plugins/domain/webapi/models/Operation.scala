package amf.plugins.domain.webapi.models

import amf.plugins.domain.webapi.models.security.ParametrizedSecurityScheme
import amf.framework.model.domain.DomainElement
import amf.framework.parser.{Annotations, Fields}
import amf.plugins.domain.webapi.metamodel.OperationModel.{Request => OperationRequest, _}
import amf.plugins.domain.webapi.metamodel.OperationModel
import amf.plugins.domain.webapi.models.templates.ParametrizedTrait

/**
  * Operation internal model.
  */
case class Operation(fields: Fields, annotations: Annotations) extends DomainElement {

  def method: String               = fields(Method)
  def name: String                 = fields(Name)
  def description: String          = fields(Description)
  def deprecated: Boolean          = fields(Deprecated)
  def summary: String              = fields(Summary)
  def documentation: CreativeWork  = fields(Documentation)
  def schemes: Seq[String]         = fields(Schemes)
  def accepts: Seq[String]         = fields(Accepts)
  def contentType: Seq[String]     = fields(ContentType)
  def request: Request             = fields(OperationRequest)
  def responses: Seq[Response]     = fields(Responses)
  def security: Seq[DomainElement] = fields(Security)

  def traits: Seq[ParametrizedTrait] = extend collect { case t: ParametrizedTrait => t }

  def withMethod(method: String): this.type                     = set(Method, method)
  def withName(name: String): this.type                         = set(Name, name)
  def withDescription(description: String): this.type           = set(Description, description)
  def withDeprecated(deprecated: Boolean): this.type            = set(Deprecated, deprecated)
  def withSummary(summary: String): this.type                   = set(Summary, summary)
  def withDocumentation(documentation: CreativeWork): this.type = set(Documentation, documentation)
  def withSchemes(schemes: Seq[String]): this.type              = set(Schemes, schemes.toList)
  def withAccepts(accepts: Seq[String]): this.type              = set(Accepts, accepts.toList)
  def withContentType(contentType: Seq[String]): this.type      = set(ContentType, contentType.toList)
  def withRequest(request: Request): this.type                  = set(OperationRequest, request)
  def withResponses(responses: Seq[Response]): this.type        = setArray(Responses, responses)
  def withSecurity(security: Seq[DomainElement]): this.type     = setArray(Security, security)

  override def adopted(parent: String): this.type = withId(parent + "/" + method)

  def withResponse(name: String): Response = {
    val result = Response().withName(name).withStatusCode(if (name == "default") "200" else name)
    add(Responses, result)
    result
  }

  def withRequest(): Request = {
    val request = Request()
    set(OperationRequest, request)
    request
  }

  def withSecurity(name: String): ParametrizedSecurityScheme = {
    val result = ParametrizedSecurityScheme().withName(name)
    add(Security, result)
    result
  }

  override def meta = OperationModel
}

object Operation {

  def apply(): Operation = apply(Annotations())

  def apply(annotations: Annotations): Operation = new Operation(Fields(), annotations)
}
