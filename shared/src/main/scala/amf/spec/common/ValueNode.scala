package amf.spec.common

import amf.common.core._
import amf.common.AMFAST
import amf.domain.Annotations
import amf.model.AmfScalar

case class ValueNode(ast: AMFAST) {

  def string(): AmfScalar = {
    val content = ast.content.unquote
    AmfScalar(content, annotations())
  }

  def integer(): AmfScalar = {
    val content = ast.content.unquote
    AmfScalar(content.toInt, annotations())
  }

  def boolean(): AmfScalar = {
    val content = ast.content.unquote
    AmfScalar(content.toBoolean, annotations())
  }

  def negated(): AmfScalar = {
    val content = ast.content.unquote
    AmfScalar(!content.toBoolean, annotations())
  }

  private def annotations() = Annotations(ast)
}
