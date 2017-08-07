package amf.model.builder

import amf.model.{CreativeWork, Operation, Request, Response}

import scala.collection.JavaConverters._

/**
  * Operation builder.
  */
case class OperationBuilder(
    private[amf] val internalBuilder: amf.builder.OperationBuilder = amf.builder.OperationBuilder())
    extends Builder {

  def withMethod(method: String): OperationBuilder = {
    internalBuilder.withMethod(method)
    this
  }

  def withName(name: String): OperationBuilder = {
    internalBuilder.withName(name)
    this
  }

  def withDescription(description: String): OperationBuilder = {
    internalBuilder.withDescription(description)
    this
  }

  def withDeprecated(deprecated: Boolean): OperationBuilder = {
    internalBuilder.withDeprecated(deprecated)
    this
  }

  def withSummary(summary: String): OperationBuilder = {
    internalBuilder.withSummary(summary)
    this
  }

  def withDocumentation(documentation: CreativeWork): OperationBuilder = {
    internalBuilder.withDocumentation(documentation.creativeWork)
    this
  }

  def withSchemes(schemes: java.util.List[String]): OperationBuilder = {
    internalBuilder.withSchemes(schemes.asScala)
    this
  }

  def withRequest(request: Request): OperationBuilder = {
    internalBuilder.withRequest(request.request)
    this
  }

  def withResponses(responses: java.util.List[Response]): OperationBuilder = {
    internalBuilder.withResponses(responses.asScala.map(_.response))
    this
  }

  def build: Operation = Operation(internalBuilder.build)
}
