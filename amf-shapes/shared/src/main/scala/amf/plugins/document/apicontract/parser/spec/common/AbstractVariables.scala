package amf.plugins.document.apicontract.parser.spec.common

import amf.core.client.scala.model.domain.AmfScalar
import amf.core.client.scala.transform.VariableReplacer.VariableRegex
import amf.core.internal.parser.domain.{Annotations, ScalarNode}
import org.yaml.model.YNode

import scala.collection.mutable

case class AbstractVariables() {
  private val variables: mutable.Map[String, Annotations] = mutable.Map()

  def parseVariables(node: YNode): this.type = parseVariables(ScalarNode(node))

  def parseVariables(scalarNode: ScalarNode): this.type = {
    VariableRegex
      .findAllMatchIn(scalarNode.text().toString)
      .foreach(m => variables.update(m.group(1), scalarNode.string().annotations))
    this
  }

  def ifNonEmpty(fn: Seq[AmfScalar] => Unit): Unit =
    if (variables.nonEmpty) fn(variables.map(v => AmfScalar(v._1, v._2)).toSeq)
}
