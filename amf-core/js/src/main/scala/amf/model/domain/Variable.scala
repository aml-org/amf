package amf.model.domain

import amf.core.model.domain.templates

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportTopLevel("model.domain.Variable")
@JSExportAll
case class Variable(private[amf] val variable: templates.Variable) {
  def name: String = variable.name
  def value: String = variable.name
}