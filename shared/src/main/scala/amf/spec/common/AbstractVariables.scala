package amf.spec.common

import amf.domain.`abstract`.Variable
import amf.metadata.domain.`abstract`.VariableModel._
import org.yaml.model.YScalar

import scala.collection.mutable
import scala.util.matching.Regex

case class AbstractVariables() {
  val variables: mutable.Map[String, Variable] = mutable.Map()
  val VariableRegex: Regex =
    "<<([^<<>>\\s]+)(?:\\s*\\|\\s*!(singularize|pluralize|uppercase|lowercase|lowercamelcase|uppercamelcase|lowerunderscorecase|upperunderscorecase|lowerhyphencase|upperhyphencase))?>>".r

  def ++=(params: Seq[Variable]): this.type = {
    variables ++= params.map(p => p.name -> p).toMap
    this
  }

  def parseVariables(scalar: YScalar): this.type = parseVariables(scalar.text)

  def parseVariables(s: String): this.type = {
    VariableRegex
      .findAllMatchIn(s)
      .foreach(m => {
        val name = m.group(1)
        val p    = Variable().set(Name, name)
        Option(m.group(2)).foreach(p.set(Transformation, _))
        variables += name -> p
      })
    this
  }

  def ifNonEmpty(fn: Seq[Variable] => Unit): Unit = if (variables.nonEmpty) fn(variables.values.toSeq)
}
