package amf.model.builder

import amf.model.{EndPoint, Operation, Parameter}

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportAll

/**
  * EndPoint builder.
  */
@JSExportAll
case class EndPointBuilder(private val endPointBuilder: amf.builder.EndPointBuilder = amf.builder.EndPointBuilder())
    extends Builder {

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

  def withOperations(operations: js.Iterable[Operation]): EndPointBuilder = {
    endPointBuilder.withOperations(operations.toList.map(_.element))
    this
  }

  def withParameters(parameters: js.Iterable[Parameter]): EndPointBuilder = {
    endPointBuilder.withParameters(parameters.toList.map(_.element))
    this
  }

  def build: EndPoint = EndPoint(endPointBuilder.build)
}
