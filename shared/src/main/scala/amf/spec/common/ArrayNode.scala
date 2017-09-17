package amf.spec.common

import amf.common.AMFAST
import amf.domain.Annotations
import amf.model.AmfArray

case class ArrayNode(ast: AMFAST) {

  def strings(): AmfArray = {
    val elements = ast.children.map(child => ValueNode(child).string())
    AmfArray(elements, annotations())
  }

  val values: Seq[AMFAST] = ast.children

  private def annotations() = Annotations(ast)
}
