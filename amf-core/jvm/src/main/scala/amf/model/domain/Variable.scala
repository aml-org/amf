package amf.model.domain

import amf.core.model.domain.templates

case class Variable(private[amf] val variable: templates.Variable) {
  def name: String = variable.name
  def value: String = variable.name
}