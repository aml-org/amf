package amf.model.builder

import amf.model.{EndPoint, Operation, Parameter}

import scala.collection.JavaConverters._

/**
  * EndPoint builder.
  */
case class EndPointBuilder private (
    private val endPointBuilder: amf.builder.EndPointBuilder = amf.builder.EndPointBuilder())
    extends Builder {

  def this() = this(amf.builder.EndPointBuilder())

  def withName(name: String): EndPointBuilder = {
    endPointBuilder.withName(name)
    this
  }

  def withDescription(description: String): EndPointBuilder = {
    endPointBuilder.withDescription(description)
    this
  }

  def withPath(path: String): EndPointBuilder = {
    endPointBuilder.withPath(path)
    this
  }

  def withOperations(operations: java.util.List[Operation]): EndPointBuilder = {
    endPointBuilder.withOperations(operations.asScala.map(_.element).toList)
    this
  }

  def withParameters(parameters: java.util.List[Parameter]): EndPointBuilder = {
    endPointBuilder.withParameters(parameters.asScala.map(_.element).toList)
    this
  }

  def build: EndPoint = EndPoint(endPointBuilder.build)
}
