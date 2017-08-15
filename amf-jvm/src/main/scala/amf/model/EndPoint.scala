package amf.model

import scala.collection.JavaConverters._

/**
  * Endpoint jvm class
  */
case class EndPoint private[model] (private val endPoint: amf.domain.EndPoint) extends DomainElement {

  val name: String                          = endPoint.name
  val description: String                   = endPoint.description
  val path: String                          = endPoint.path
  val operations: java.util.List[Operation] = endPoint.operations.map(Operation).asJava
  val parameters: java.util.List[Parameter] = endPoint.parameters.map(Parameter).asJava

  override def equals(other: Any): Boolean = other match {
    case that: EndPoint =>
      (that canEqual this) &&
        endPoint == that.endPoint
    case _ => false
  }

  def canEqual(other: Any): Boolean = other.isInstanceOf[EndPoint]

  override private[amf] def element: amf.domain.EndPoint = endPoint
}
