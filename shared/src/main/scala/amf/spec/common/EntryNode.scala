package amf.spec.common

import amf.common.AMFAST
import amf.domain.Annotations

trait KeyValueNode {
  val key: AMFAST
  val value: AMFAST
  val ast: AMFAST

  def annotations(): Annotations
}

case class EntryNode(ast: AMFAST) extends KeyValueNode {

  val key: AMFAST   = ast.head
  val value: AMFAST = Option(ast).filter(_.children.size > 1).map(_.last).orNull

  def annotations(): Annotations = Annotations(ast)
}
