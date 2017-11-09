package amf.spec.common

import amf.domain._
import amf.model.{AmfArray, AmfScalar}
import amf.parser.YValueOps
import amf.spec.ParserContext
import org.yaml.model._

/**
  * Base spec parser.
  */
private[spec] trait BaseSpecParser {

  implicit val ctx: ParserContext

}

case class ArrayNode(ast: YSequence)(implicit iv: IllegalTypeHandler) {

  def strings(): AmfArray = {
    val elements = ast.nodes.map(child => ValueNode(child).string())
    AmfArray(elements, annotations())
  }

  private def annotations() = Annotations(ast)
}

case class ValueNode(node: YNode)(implicit iv: IllegalTypeHandler) {

  def string(): AmfScalar = {
    val content = node.as[String]
    AmfScalar(content, annotations())
  }

  def text(): AmfScalar = {
    val content = node.value.toScalar.text
    AmfScalar(content, annotations())
  }

  def integer(): AmfScalar = {
    val content = node.as[Int]
    AmfScalar(content, annotations())
  }

  def boolean(): AmfScalar = {
    val content = node.as[Boolean]
    AmfScalar(content, annotations())
  }

  def negated(): AmfScalar = {
    val content = node.as[Boolean]
    AmfScalar(!content, annotations())
  }

  private def annotations() = Annotations(node)
}
