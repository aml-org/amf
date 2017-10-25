package amf.spec.common

import amf.domain._
import amf.model.{AmfArray, AmfScalar}
import amf.parser.YValueOps
import org.yaml.model._

/**
  * Base spec parser.
  */
private[spec] trait BaseSpecParser {

  implicit val spec: SpecParserContext

}

trait SpecParserContext {
  def link(node: YNode): Either[String, YNode]
}

case class ArrayNode(ast: YSequence) {

  def strings(): AmfArray = {
    val elements = ast.nodes.map(child => ValueNode(child).string())
    AmfArray(elements, annotations())
  }

  private def annotations() = Annotations(ast)
}

case class ValueNode(ast: YNode) {

  def string(): AmfScalar = {
    val content = scalar.text
    AmfScalar(content, annotations())
  }

  def integer(): AmfScalar = {
    val content = scalar.text
    AmfScalar(content.toInt, annotations())
  }

  def boolean(): AmfScalar = {
    val content = scalar.text
    AmfScalar(content.toBoolean, annotations())
  }

  def negated(): AmfScalar = {
    val content = scalar.text
    AmfScalar(!content.toBoolean, annotations())
  }

  private def scalar = ast.value.toScalar

  private def annotations() = Annotations(ast)
}
