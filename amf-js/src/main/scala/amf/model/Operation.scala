package amf.model

import amf.model.builder.OperationBuilder

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.JSExportAll

/**
  * Operations js class
  */
@JSExportAll
case class Operation private[model] (private[amf] val operation: amf.domain.Operation) extends DomainElement {

  val method: String = operation.method

  val name: String = operation.name

  val description: String = operation.description

  val deprecated: Boolean = operation.deprecated

  val summary: String = operation.summary

  val documentation: CreativeWork = CreativeWork(operation.documentation)

  val schemes: js.Iterable[String] = operation.schemes.toJSArray

  val request: Request = Request(operation.request)

  val responses: js.Iterable[Response] = operation.responses.map(Response).toJSArray

  def toBuilder: OperationBuilder = OperationBuilder(operation.toBuilder)
}
