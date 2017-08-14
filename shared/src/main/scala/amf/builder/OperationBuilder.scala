package amf.builder

import amf.domain._
import amf.metadata.domain.OperationModel.{Request => OperationRequest, _}

/**
  * Operation builder.
  */
class OperationBuilder extends Builder {

  override type T = Operation

  def withMethod(method: String): OperationBuilder = set(Method, method)

  def withName(name: String): OperationBuilder = set(Name, name)

  def withDescription(description: String): OperationBuilder = set(Description, description)

  def withDeprecated(deprecated: Boolean): OperationBuilder = set(Deprecated, deprecated)

  def withSummary(summary: String): OperationBuilder = set(Summary, summary)

  def withDocumentation(documentation: CreativeWork): OperationBuilder = set(Documentation, documentation)

  def withSchemes(schemes: Seq[String]): OperationBuilder = set(Schemes, schemes.toList)

  def withRequest(request: Request): OperationBuilder = set(OperationRequest, request)

  def withResponses(responses: Seq[Response]): OperationBuilder = set(Responses, responses)

  override def resolveId(container: String): this.type = {
    val method: String = fields(Method)
    withId(container + "/" + method)
  }

  override def build: Operation = Operation(fields, annotations)
}

object OperationBuilder {
  def apply(): OperationBuilder = apply(Nil)

  def apply(fields: Fields, annotations: List[Annotation] = Nil): OperationBuilder = apply(annotations).copy(fields)

  def apply(annotations: List[Annotation]): OperationBuilder = new OperationBuilder().withAnnotations(annotations)
}
