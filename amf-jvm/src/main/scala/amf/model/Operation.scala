package amf.model

import amf.model.builder.OperationBuilder

import scala.collection.JavaConverters._

/**
  * Operation jvm class
  */
case class Operation private[model] (private[amf] val operation: amf.domain.Operation) extends DomainElement {

  val method: String = operation.method

  val name: String = operation.name

  val description: String = operation.description

  val deprecated: Boolean = operation.deprecated

  val summary: String = operation.summary

  val documentation: CreativeWork = CreativeWork(operation.documentation)

  val schemes: java.util.List[String] = operation.schemes.asJava

  val request: Request = Request(operation.request)

  val responses: java.util.List[Response] = operation.responses.map(Response).asJava

  def toBuilder: OperationBuilder = OperationBuilder(operation.toBuilder)
}
