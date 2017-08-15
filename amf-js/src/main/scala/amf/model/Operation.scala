package amf.model

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.JSExportAll

/**
  * Operations js class
  */
@JSExportAll
case class Operation private[model] (private val operation: amf.domain.Operation) extends DomainElement {

  val method: String      = operation.method
  val name: String        = operation.name
  val description: String = operation.description
  val deprecated: Boolean = operation.deprecated
  val summary: String     = operation.summary
  val documentation: CreativeWork =
    if (operation.documentation != null) CreativeWork(operation.documentation) else null
  val schemes: js.Iterable[String] = operation.schemes.toJSArray
  val request: Request =
    if (operation.request != null) Request(operation.request) else null
  val responses: js.Iterable[Response] = operation.responses.map(Response).toJSArray

  override def equals(other: Any): Boolean = other match {
    case that: Operation =>
      (that canEqual this) &&
        operation == that.operation
    case _ => false
  }

  def canEqual(other: Any): Boolean = other.isInstanceOf[Operation]

  override private[amf] def element: amf.domain.Operation = operation
}
