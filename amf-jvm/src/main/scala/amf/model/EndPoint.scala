package amf.model

import amf.model.builder.EndPointBuilder

import scala.collection.JavaConverters._

/**
  * Endpoint jvm class
  */
case class EndPoint private[model] (private val endPoint: amf.domain.EndPoint) extends DomainElement {

  val name: String = endPoint.name

  val description: String = endPoint.description

  val path: String = endPoint.path

  val operations: java.util.List[Operation] = endPoint.operations.map(Operation).asJava

  val parameters: java.util.List[Parameter] = endPoint.parameters.map(Parameter).asJava

  def toBuilder: EndPointBuilder = EndPointBuilder(endPoint.toBuilder)
}
