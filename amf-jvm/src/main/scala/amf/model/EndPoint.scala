package amf.model

import scala.collection.JavaConverters._

/**
  * Endpoint jvm class
  */
case class EndPoint private[model] (private val endPoint: amf.domain.EndPoint) extends DomainElement {

  def this() = this(amf.domain.EndPoint())

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
  def withOperations(operations: java.util.List[Operation]): this.type = {
    endPoint.withOperations(operations.asScala.map(_.element))
    this
  }
  def withParameters(parameters: java.util.List[Parameter]): this.type = {
    endPoint.withParameters(parameters.asScala.map(_.element))
    this
  }

  def withOperation(method: String): Operation = Operation(endPoint.withOperation(method))

  def withParameter(name: String): Parameter = Parameter(endPoint.withParameter(name))

}
