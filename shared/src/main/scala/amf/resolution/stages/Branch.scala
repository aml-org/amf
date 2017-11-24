package amf.resolution.stages

import amf.framework.model.document.BaseUnit
import amf.framework.model.domain.templates.{Variable, VariableValue}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

case class BranchContainer(branches: Seq[Branch]) {
  def flatten(): Seq[Branch] = {
    val visited: mutable.Set[Key]      = mutable.Set()
    val queue: ListBuffer[Seq[Branch]] = ListBuffer()
    queue += branches
    visited ++= branches.map(_.key)
    collect(visited, queue, branches)
    queue.foldLeft(Seq[Branch]())(_ ++ _)
  }

  private def collect(visited: mutable.Set[Key], queue: ListBuffer[Seq[Branch]], previous: Seq[Branch]): Unit = {
    val branches: ListBuffer[Branch] = ListBuffer()
    previous.foreach(prev => {
      prev.children.filter(c => visited.add(c.key)).foreach(branches += _)
    })
    queue += branches
    if (branches.nonEmpty) collect(visited, queue, branches)
  }
}

object BranchContainer {
  def merge(left: Seq[Branch], right: Seq[Branch]): Seq[Branch] = left ++ right.filterNot(left.contains(_))
}

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
