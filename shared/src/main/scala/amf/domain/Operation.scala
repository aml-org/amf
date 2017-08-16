package amf.domain
import amf.common.AMFAST
import amf.metadata.domain.OperationModel.{Request => OperationRequest, _}

/**
  * Operation internal model.
  */
case class Operation(fields: Fields, annotations: Annotations) extends DomainElement {

  def method: String              = fields(Method)
  def name: String                = fields(Name)
  def description: String         = fields(Description)
  def deprecated: Boolean         = fields(Deprecated)
  def summary: String             = fields(Summary)
  def documentation: CreativeWork = fields(Documentation)
  def schemes: Seq[String]        = fields(Schemes)
  def request: Request            = fields(OperationRequest)
  def responses: Seq[Response]    = fields(Responses)

  def withMethod(method: String): this.type                     = set(Method, method)
  def withName(name: String): this.type                         = set(Name, name)
  def withDescription(description: String): this.type           = set(Description, description)
  def withDeprecated(deprecated: Boolean): this.type            = set(Deprecated, deprecated)
  def withSummary(summary: String): this.type                   = set(Summary, summary)
  def withDocumentation(documentation: CreativeWork): this.type = set(Documentation, documentation)
  def withSchemes(schemes: Seq[String]): this.type              = set(Schemes, schemes.toList)
  def withRequest(request: Request): this.type                  = set(OperationRequest, request)
  def withResponses(responses: Seq[Response]): this.type        = setArray(Responses, responses)
}

object Operation {
  def apply(): Operation = new Operation(Fields(), Annotations())

  def apply(ast: AMFAST): Operation = new Operation(Fields(), Annotations(ast))
}
