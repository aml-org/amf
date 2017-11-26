package amf.model.domain

import amf.core.model.domain.templates

/**
  * JS VariableValue model class.
  */
case class VariableValue private[model] (private[amf] val variable: templates.VariableValue)
    extends DomainElement {
  def this() = this(templates.VariableValue())

  val name: String  = variable.name
  val value: String = variable.value

  def withName(name: String): this.type = {
    variable.withName(name)
    this
  }

  def withValue(value: String): this.type = {
    variable.withValue(value)
    this
  }

  override def element: templates.VariableValue = variable
}
