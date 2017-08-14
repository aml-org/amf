package amf.model

import amf.model.builder.{CreativeWorkBuilder, OperationBuilder, RequestBuilder}

import scala.collection.JavaConverters._

/**
  * Operation jvm class
  */
case class Operation private[model] (private val operation: amf.domain.Operation) extends DomainElement {

  val method: String = operation.method

  val name: String = operation.name

  val description: String = operation.description

  val deprecated: Boolean = operation.deprecated

  val summary: String = operation.summary

  val documentation: CreativeWork =
    if (operation.documentation != null) CreativeWork(operation.documentation) else CreativeWorkBuilder().build

  val schemes: java.util.List[String] = operation.schemes.asJava

  val request: Request =
    if (operation.request != null) Request(operation.request) else RequestBuilder().build

  val responses: java.util.List[Response] = operation.responses.map(Response).asJava

  def toBuilder: OperationBuilder = OperationBuilder(operation.toBuilder)

  override def equals(other: Any): Boolean = other match {
    case that: Operation =>
      (that canEqual this) &&
        operation == that.operation
    case _ => false
  }

  def canEqual(other: Any): Boolean = other.isInstanceOf[Operation]

  override private[amf] def element: amf.domain.Operation = operation
}
