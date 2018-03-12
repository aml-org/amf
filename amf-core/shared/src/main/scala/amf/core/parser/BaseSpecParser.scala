package amf.core.parser

import amf.core.model.domain.AmfScalar
import org.yaml.model._

/**
  * Base spec parser.
  */
trait BaseSpecParser {

  implicit val ctx: ParserContext

}

/*
case class ArrayNode(ast: YNode)(implicit iv: IllegalTypeHandler) {

  def strings(): AmfArray = {
    val nodes    = ast.as[Seq[String]]
    val elements = nodes.map(child => AmfScalar(child, annotations()))

    AmfArray(elements, annotations())
  }

  def rawMembers(): AmfArray = {
    val nodes    = ast.as[Seq[YNode]]
    val elements = nodes.map(child => AmfScalar(child.toString, annotations()))

    AmfArray(elements, annotations())
  }

  private def annotations() = Annotations(ast.value)
}
*/

case class ValueNode(node: YNode)(implicit iv: IllegalTypeHandler) {

  def string(): AmfScalar = {
    val content = node.as[String]
    AmfScalar(content, annotations())
  }

  def text(): AmfScalar = {
    val content = node.as[YScalar].text
    AmfScalar(content, annotations())
  }

  def integer(): AmfScalar = {
    val content = node.as[Int]
    AmfScalar(content, annotations())
  }

  def float(): AmfScalar = {
    val content = node.as[Double]
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

  private def annotations() = Annotations(node.value)
}
