package amf.plugins.document.webapi.parser.spec.common

import amf.resolution.stages.VariableReplacer.VariableRegex
import org.yaml.model.YScalar

import scala.collection.mutable

case class AbstractVariables() {
  val variables: mutable.Set[String] = mutable.Set()

  def parseVariables(scalar: YScalar): this.type = parseVariables(scalar.text)

  def parseVariables(s: String): this.type = {
    VariableRegex
      .findAllMatchIn(s)
      .foreach(m => variables += m.group(1))
    this
  }

  def ifNonEmpty(fn: Seq[String] => Unit): Unit = if (variables.nonEmpty) fn(variables.toSeq)
}
