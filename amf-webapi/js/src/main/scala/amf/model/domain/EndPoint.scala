package amf.model.domain

import amf.plugins.domain.webapi.models

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * JS EndPoint model class.
  */
@JSExportAll
case class EndPoint private[model] (private val endPoint: models.EndPoint) extends DomainElement {

  @JSExportTopLevel("model.domain.EndPoint")
  def this() = this(models.EndPoint())

  def name: String                       = endPoint.name
  def description: String                = endPoint.description
  def path: String                       = endPoint.path
  def operations: js.Iterable[Operation] = Option(endPoint.operations).getOrElse(Nil).map(Operation).toJSArray
  def parameters: js.Iterable[Parameter] = Option(endPoint.parameters).getOrElse(Nil).map(Parameter).toJSArray
  def security: js.Iterable[ParametrizedSecurityScheme] =
    Option(endPoint.security).getOrElse(Nil).map(ParametrizedSecurityScheme).toJSArray

  override private[amf] def element: models.EndPoint = endPoint

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

  /** Set security property of this [[EndPoint]] using a list of [[ParametrizedSecurityScheme]]. */
  def withSecurity(security: js.Iterable[ParametrizedSecurityScheme]): this.type = {
    endPoint.withSecurity(security.toSeq.map(_.element))
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
