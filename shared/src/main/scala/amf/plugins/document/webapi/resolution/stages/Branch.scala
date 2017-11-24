package amf.plugins.document.webapi.resolution.stages

import amf.framework.model.document.BaseUnit
import amf.framework.model.domain.templates.{Variable, VariableValue}





trait Branch {
  val key: Key
  val children: Seq[Branch]
}

case class Key(name: String, variables: Set[Variable])

object Key {
  def apply(name: String, variables: Set[Variable]): Key = new Key(name, variables)

  def apply(name: String, context: Context): Key = {
    Key(name, context.variables)
  }
}

case class Context(model: BaseUnit, variables: Set[Variable] = Set()) {

  def add(name: String, value: String): Context = copy(variables = variables + Variable(name, value))

  def add(vs: Seq[VariableValue]): Context = copy(variables = variables ++ vs.map(v => Variable(v.name, v.value)))
}
