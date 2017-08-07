package amf.model

import amf.model.builder.ParameterBuilder

import scala.scalajs.js.annotation.JSExportAll

/**
  * parameter js class
  */
@JSExportAll
case class Parameter private[model] (private[amf] val parameter: amf.domain.Parameter) extends DomainElement {

  val name: String = parameter.name

  val description: String = parameter.description

  val required: Boolean = parameter.required

  val binding: String = parameter.binding

  val schema: String = parameter.schema

  def toBuilder: ParameterBuilder = ParameterBuilder(parameter.toBuilder)
}
