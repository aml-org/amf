package amf.model.builder

import amf.model.{CreativeWork, Operation, Request, Response}

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportAll

/**
  * Operation builder.
  */
@JSExportAll
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
    internalBuilder.withDocumentation(documentation.element)
    this
  }

  def withSchemes(schemes: js.Iterable[String]): OperationBuilder = {
    internalBuilder.withSchemes(schemes.toList)
    this
  }

  def withRequest(request: Request): OperationBuilder = {
    internalBuilder.withRequest(request.element)
    this
  }

  def withResponses(responses: js.Iterable[Response]): OperationBuilder = {
    internalBuilder.withResponses(responses.toList.map(_.element))
    this
  }

  def build: Operation = Operation(internalBuilder.build)
}
