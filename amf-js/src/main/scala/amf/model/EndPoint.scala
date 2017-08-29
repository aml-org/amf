package amf.model

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.JSExportAll

/**
  * JS EndPoint model class.
  */
@JSExportAll
case class EndPoint private[model] (private val endPoint: amf.domain.EndPoint) extends DomainElement {

  def this() = this(amf.domain.EndPoint())

  val name: String                       = endPoint.name
  val description: String                = endPoint.description
  val path: String                       = endPoint.path
  val operations: js.Iterable[Operation] = endPoint.operations.map(Operation).toJSArray
  val parameters: js.Iterable[Parameter] = endPoint.parameters.map(Parameter).toJSArray

  override def equals(other: Any): Boolean = other match {
    case that: EndPoint =>
      (that canEqual this) &&
        endPoint == that.endPoint
    case _ => false
  }

  def canEqual(other: Any): Boolean = other.isInstanceOf[EndPoint]

  override private[amf] def element: amf.domain.EndPoint = endPoint

  /** Get the part of the path property that was defined by this [[EndPoint]]. */
  def relativePath: String = endPoint.relativePath

  /** Set name property of this [[EndPoint]]. */
  def withName(name: String): this.type = {
    endPoint.withName(name)
    this
  }

  /** Set description property of this [[EndPoint]]. */
  def withDescription(description: String): this.type = {
    endPoint.withDescription(description)
    this
  }

  /** Set full path property of this [[EndPoint]]. */
  def withPath(path: String): this.type = {
    endPoint.withPath(path)
    this
  }

  /** Set operations property of this [[EndPoint]]. */
  def withOperations(operations: js.Iterable[Operation]): this.type = {
    endPoint.withOperations(operations.toSeq.map(_.element))
    this
  }

  /** Set parameters property of this [[EndPoint]]. */
  def withParameters(parameters: js.Iterable[Parameter]): this.type = {
    endPoint.withParameters(parameters.toSeq.map(_.element))
    this
  }

  /**
    * Adds one [[Operation]] to the operations property of this [[EndPoint]] and returns it for population.
    * Method property of the operation is required.
    */
  def withOperation(method: String): Operation = Operation(endPoint.withOperation(method))

  /**
    * Adds one [[Parameter]] to the uriParameters property of this [[EndPoint]] and returns it for population.
    * Name property of the parameter is required.
    */
  def withParameter(name: String): Parameter = Parameter(endPoint.withParameter(name))
}
