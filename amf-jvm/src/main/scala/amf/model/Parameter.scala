package amf.model

import amf.model.builder.ParameterBuilder

/**
  * Parameter jvm class
  */
case class Parameter private[model] (private[amf] val parameter: amf.domain.Parameter) extends DomainElement {

  val name: String = parameter.name

  val description: String = parameter.description

  val required: Boolean = parameter.required

  val binding: String = parameter.binding

  val schema: String = parameter.schema

  def toBuilder: ParameterBuilder = ParameterBuilder(parameter.toBuilder)

  override def equals(other: Any): Boolean = other match {
    case that: Parameter =>
      (that canEqual this) &&
        parameter == that.parameter
    case _ => false
  }

  def canEqual(other: Any): Boolean = other.isInstanceOf[Parameter]
}
