package amf.model.domain

import amf.core.model.domain.templates

/**
  * JS VariableValue model class.
  */
case class VariableValue private[model] (private[amf] val variable: templates.VariableValue) extends DomainElement {
  def this() = this(templates.VariableValue())

  def name: String    = variable.name
  def value: DataNode = platform.wrap(variable.value)

  def withName(name: String): this.type = {
    variable.withName(name)
    this
  }

  def withValue(value: DataNode): this.type = {
    variable.withValue(value.dataNode)
    this
  }

  override def element: templates.VariableValue = variable
}
