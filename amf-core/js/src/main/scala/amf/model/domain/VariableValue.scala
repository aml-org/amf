package amf.model.domain

import amf.core.model.domain.templates

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * JS VariableValue model class.
  */
@JSExportTopLevel("model.domain.VariableValue")
@JSExportAll
case class VariableValue(private[amf] val variable: templates.VariableValue) extends DomainElement {

  @JSExportTopLevel("model.domain.VariableValue")
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
