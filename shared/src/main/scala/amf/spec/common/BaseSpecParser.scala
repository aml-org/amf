package amf.spec.common

import amf.domain.Annotation.LexicalInformation
import amf.domain._
import amf.model.{AmfArray, AmfScalar}
import amf.parser.YValueOps
import amf.remote.Vendor
import amf.validation.{SeverityLevels, Validation}
import amf.vocabulary.Namespace
import org.yaml.model._

/**
  * Base spec parser.
  */

trait ErrorReporterParser {
  def parsingErrorReport(currentValidation: Validation, id: String, message: String, ast: Option[YPart], severity: String = SeverityLevels.VIOLATION): Unit = {
    val pos = ast match {
      case Some(node) => Some(LexicalInformation(amf.parser.Range(node.range)))
      case _          => None
    }
    currentValidation.reportConstraintFailure(
      severity,
      (Namespace.AmfParser + "parsingError").iri(),
      id,
      None,
      message,
      pos
    )
  }
}

private[spec] trait BaseSpecParser extends ErrorReporterParser {

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
