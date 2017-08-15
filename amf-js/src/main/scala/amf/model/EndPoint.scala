package amf.model

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.JSExportAll

/**
  * EndPoints js class
  */
@JSExportAll
case class EndPoint private[model] (private val endPoint: amf.domain.EndPoint) extends DomainElement {

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
}
