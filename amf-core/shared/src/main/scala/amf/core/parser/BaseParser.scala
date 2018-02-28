package amf.core.parser

import amf.core.model.domain.{AmfArray, AmfScalar, DomainElement}
import org.yaml.model._

/**
  * ArrayNode helper.
  */
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

/** Scalar node. */
trait ValueNode {

  /** Returns string amf scalar of string node. */
  def string(): AmfScalar

  /** Returns string amf scalar of any scalar node. */
  def text(): AmfScalar

  /** Returns integer amf scalar of integer node. */
  def integer(): AmfScalar

  /** Returns boolean amf scalar of boolean node. */
  def boolean(): AmfScalar

  /** Returns negated boolean amf scalar of boolean node. */
  def negated(): AmfScalar

  /** Collect custom domain properties of scalar (if any) to parent element. */
  def collectCustomDomainProperties(parent: DomainElement): Unit = {}
}

object ValueNode {
  def apply(node: YNode)(implicit iv: IllegalTypeHandler): ValueNode = ScalarNode(node)
}

/** Simple scalar node. */
case class ScalarNode(node: YNode)(implicit iv: IllegalTypeHandler) extends ValueNode {

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
