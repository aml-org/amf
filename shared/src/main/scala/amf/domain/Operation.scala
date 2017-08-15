package amf.domain
import amf.common.AMFAST
import amf.metadata.domain.OperationModel.{Request => OperationRequest, _}

/**
  * Operation internal model.
  */
case class Operation(fields: Fields, annotations: Annotations) extends DomainElement {

  val method: String              = fields(Method)
  val name: String                = fields(Name)
  val description: String         = fields(Description)
  val deprecated: Boolean         = fields(Deprecated)
  val summary: String             = fields(Summary)
  val documentation: CreativeWork = fields(Documentation)
  val schemes: Seq[String]        = fields(Schemes)
  val request: Request            = fields(OperationRequest)
  val responses: Seq[Response]    = fields(Responses)

  def withMethod(method: String): this.type                     = set(Method, method)
  def withName(name: String): this.type                         = set(Name, name)
  def withDescription(description: String): this.type           = set(Description, description)
  def withDeprecated(deprecated: Boolean): this.type            = set(Deprecated, deprecated)
  def withSummary(summary: String): this.type                   = set(Summary, summary)
  def withDocumentation(documentation: CreativeWork): this.type = set(Documentation, documentation)
  def withSchemes(schemes: Seq[String]): this.type              = set(Schemes, schemes.toList)
  def withRequest(request: Request): this.type                  = set(OperationRequest, request)
  def withResponses(responses: Seq[Response]): this.type        = set(Responses, responses)
}

object Operation {
  def apply(fields: Fields = Fields(), annotations: Annotations = new Annotations()): Operation =
    new Operation(fields, annotations)

  def apply(ast: AMFAST): Operation = new Operation(Fields(), Annotations(ast))
}
