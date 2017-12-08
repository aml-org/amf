package amf.model.domain

import amf.core.model.domain.templates

import scala.scalajs.js.annotation.JSExportAll

/**
  * JS VariableValue model class.
  */
@JSExportAll
case class VariableValue private[model] (private val variable: templates.VariableValue)
    extends DomainElement {
  def this() = this(templates.VariableValue())

  def name: String  = variable.name
  def value: String = variable.value

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
