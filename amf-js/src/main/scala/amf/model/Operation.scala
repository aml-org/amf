package amf.model

import amf.model.builder.{CreativeWorkBuilder, OperationBuilder, RequestBuilder}

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.JSExportAll

/**
  * Operations js class
  */
@JSExportAll
case class Operation private[model] (private val operation: amf.domain.Operation) extends DomainElement {

  val method: String = operation.method

  val name: String = operation.name

  val description: String = operation.description

  val deprecated: Boolean = operation.deprecated

  val summary: String = operation.summary

  val documentation: CreativeWork =
    if (operation.documentation != null) CreativeWork(operation.documentation) else CreativeWorkBuilder().build

  val schemes: js.Iterable[String] = operation.schemes.toJSArray

  val request: Request =
    if (operation.request != null) Request(operation.request) else RequestBuilder().build

  val responses: js.Iterable[Response] = operation.responses.map(Response).toJSArray

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
