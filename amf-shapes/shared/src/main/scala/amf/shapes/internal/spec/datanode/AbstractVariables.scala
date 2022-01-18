package amf.shapes.internal.spec.datanode

import amf.core.client.scala.model.domain.AmfScalar
import amf.core.internal.datanode.OnScalarParseHook
import amf.core.internal.transform.VariableReplacer.VariableRegex
import amf.core.internal.parser.domain.{Annotations, ScalarNode}
import org.yaml.model.YNode

import scala.collection.mutable

case class AbstractVariables() extends OnScalarParseHook {
  private val variables: mutable.Map[String, Annotations] = mutable.Map()

  override def onScalarParse(node: YNode): Unit      = parseVariables(node)
  override def onScalarParse(node: ScalarNode): Unit = parseVariables(node)

  private def parseVariables(node: YNode): this.type = parseVariables(ScalarNode(node))

  private def parseVariables(scalarNode: ScalarNode): this.type = {
    VariableRegex
      .findAllMatchIn(scalarNode.text().toString)
      .foreach(m => variables.update(m.group(1), scalarNode.string().annotations))
    this
  }

  def ifNonEmpty(fn: Seq[AmfScalar] => Unit): Unit =
    if (variables.nonEmpty) fn(variables.map(v => AmfScalar(v._1, v._2)).toSeq)
}
