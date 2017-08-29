package amf.model

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.JSExportAll

/**
  * JS Operation model class.
  */
@JSExportAll
case class Operation private[model] (private val operation: amf.domain.Operation) extends DomainElement {

  def this() = this(amf.domain.Operation())

  val method: String                   = operation.method
  val name: String                     = operation.name
  val description: String              = operation.description
  val deprecated: Boolean              = operation.deprecated
  val summary: String                  = operation.summary
  val documentation: CreativeWork      = Option(operation.documentation).map(CreativeWork).orNull
  val schemes: js.Iterable[String]     = operation.schemes.toJSArray
  val request: Request                 = Option(operation.request).map(Request).orNull
  val responses: js.Iterable[Response] = operation.responses.map(Response).toJSArray

  override def equals(other: Any): Boolean = other match {
    case that: Operation =>
      (that canEqual this) &&
        operation == that.operation
    case _ => false
  }

  def canEqual(other: Any): Boolean = other.isInstanceOf[Operation]

  override private[amf] def element: amf.domain.Operation = operation

  /** Set method property of this [[Operation]]. */
  def withMethod(method: String): this.type = {
    operation.withMethod(method)
    this
  }

  /** Set name property of this [[Operation]]. */
  def withName(name: String): this.type = {
    operation.withName(name)
    this
  }

  /** Set description property of this [[Operation]]. */
  def withDescription(description: String): this.type = {
    operation.withDescription(description)
    this
  }

  /** Set deprecated property of this [[Operation]]. */
  def withDeprecated(deprecated: Boolean): this.type = {
    operation.withDeprecated(deprecated)
    this
  }

  /** Set summary property of this [[Operation]]. */
  def withSummary(summary: String): this.type = {
    operation.withSummary(summary)
    this
  }

  /** Set documentation property of this [[Operation]] using a [[CreativeWork]]. */
  def withDocumentation(documentation: CreativeWork): this.type = {
    operation.withDocumentation(documentation.element)
    this
  }

  /** Set schemes property of this [[Operation]]. */
  def withSchemes(schemes: js.Iterable[String]): this.type = {
    operation.withSchemes(schemes.toSeq)
    this
  }

  /** Set request property of this [[Operation]]. */
  def withRequest(request: Request): this.type = {
    operation.withRequest(request.element)
    this
  }

  /** Set responses property of this [[Operation]]. */
  def withResponses(responses: js.Iterable[Response]): this.type = {
    operation.withResponses(responses.toSeq.map(_.element))
    this
  }

  /**
    * Adds one [[Response]] to the responses property of this [[Operation]] and returns it for population.
    * Name property of the response is required.
    */
  def withResponse(name: String): Response = Response(operation.withResponse(name))
}
