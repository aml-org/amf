package amf.builder

import amf.domain._
import amf.metadata.domain.OperationModel.{Request => OperationRequest, _}

/**
  * Operation builder.
  */
class OperationBuilder extends Builder {

  override type T = Operation

  def withMethod(method: String): this.type = set(Method, method)

  def withName(name: String): this.type = set(Name, name)

  def withDescription(description: String): this.type = set(Description, description)

  def isDeprecated(deprecated: Boolean): this.type = set(Deprecated, deprecated)

  def withSummary(summary: String): this.type = set(Summary, summary)

  def withDocumentation(documentation: CreativeWork): this.type = set(Documentation, documentation)

  def withSchemes(schemes: Seq[String]): this.type = set(Schemes, schemes)

  def withRequest(request: Request): this.type = set(OperationRequest, request)

  def withResponses(responses: Seq[Response]): this.type = set(Responses, responses)

  override def build: Operation = Operation(fields)
}

object OperationBuilder {
  def apply(): OperationBuilder = new OperationBuilder()

  def apply(fields: Fields): OperationBuilder = apply().copy(fields)
}
