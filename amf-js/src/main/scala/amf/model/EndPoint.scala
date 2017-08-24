package amf.model

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.JSExportAll

/**
  * EndPoints js class
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

  def relativePath: String = endPoint.relativePath

  def withName(name: String): this.type = {
    endPoint.withName(name)
    this
  }
  def withDescription(description: String): this.type = {
    endPoint.withDescription(description)
    this
  }
  def withPath(path: String): this.type = {
    endPoint.withPath(path)
    this
  }
  def withOperations(operations: js.Iterable[Operation]): this.type = {
    endPoint.withOperations(operations.toSeq.map(_.element))
    this
  }
  def withParameters(parameters: js.Iterable[Parameter]): this.type = {
    endPoint.withParameters(parameters.toSeq.map(_.element))
    this
  }

  def withOperation(method: String): Operation = Operation(endPoint.withOperation(method))

  def withParameter(name: String): Parameter = Parameter(endPoint.withParameter(name))
}
