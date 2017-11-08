package amf.spec.common

import amf.domain.Annotation.LexicalInformation
import amf.domain._
import amf.model.{AmfArray, AmfScalar}
import amf.parser.{Range, YValueOps}
import amf.remote.Vendor
import amf.validation.model.ParserSideValidations
import amf.validation.{SeverityLevels, Validation, ValidationAware}
import org.mulesoft.lexer.InputRange
import org.yaml.model._

/**
  * Base spec parser.
  */
trait ErrorReporterParser {
  def parsingErrorReport(currentValidation: Validation,
                         id: String,
                         message: String,
                         ast: Option[YPart],
                         severity: String = SeverityLevels.VIOLATION): Unit = {
    val pos = ast.map(_.range) flatMap {
      case InputRange.Zero => None
      case range           => Some(LexicalInformation(Range(range)))
    }
    currentValidation.reportConstraintFailure(
      severity,
      ParserSideValidations.ParsingErrorSpecification.id(),
      id,
      None,
      message,
      pos
    )
  }
}

private[spec] trait BaseSpecParser extends ValidationAware {

  implicit val spec: SpecParserContext

}

trait SpecParserContext {
  def link(node: YNode): Either[String, YNode]
  val vendor: Vendor
}

case class ArrayNode(ast: YSequence) {

  def strings(): AmfArray = {
    val elements = ast.nodes.map(child => ValueNode(child).string())
    AmfArray(elements, annotations())
  }

  private def annotations() = Annotations(ast)
}

case class ValueNode(node: YNode) {

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

  /*implicit val toScalar = new YRead[YScalar] {
    override def read(node: YNode): Either[YError, YScalar] = {
      val value = node.value
      if (!value.isInstanceOf[YScalar]) error(node, s"Expecting scalar and ${node.tagType} provided")
      else Right(value.asInstanceOf[YScalar])
    }

    override def defaultValue: YScalar = null
  }*/

  private def annotations() = Annotations(node)
}
