package amf.model

import scala.collection.JavaConverters._

/**
  * JVM EndPoint model class.
  */
case class EndPoint private[model] (private val endPoint: amf.domain.EndPoint) extends DomainElement {

  def this() = this(amf.domain.EndPoint())

  val name: String                          = endPoint.name
  val description: String                   = endPoint.description
  val path: String                          = endPoint.path
  val operations: java.util.List[Operation] = endPoint.operations.map(Operation).asJava
  val parameters: java.util.List[Parameter] = endPoint.parameters.map(Parameter).asJava

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
  def withOperations(operations: java.util.List[Operation]): this.type = {
    endPoint.withOperations(operations.asScala.map(_.element))
    this
  }

  /** Set parameters property of this [[EndPoint]]. */
  def withParameters(parameters: java.util.List[Parameter]): this.type = {
    endPoint.withParameters(parameters.asScala.map(_.element))
    this
  }

  /**
    * Adds one [[Operation]] to the operations property of this [[EndPoint]] and returns it for population.
    * method property of the operation is required.
    */
  def withOperation(method: String): Operation = Operation(endPoint.withOperation(method))

  /**
    * Adds one [[Parameter]] to the uriParameters property of this [[EndPoint]] and returns it for population.
    * Name property of the parameter is required.
    */
  def withParameter(name: String): Parameter = Parameter(endPoint.withParameter(name))
}
