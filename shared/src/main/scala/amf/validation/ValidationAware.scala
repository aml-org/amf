package amf.validation

import amf.domain.Annotation.LexicalInformation
import amf.parser.Range
import amf.validation.SeverityLevels.VIOLATION
import amf.validation.model.ParserSideValidations.ParsingErrorSpecification
import org.mulesoft.lexer.InputRange
import org.yaml.model._

/**
  * Validation aware: requires an implicit validation.
  */
trait ValidationAware {

  /* Implicit validation. */
  implicit val validation: Validation

  /* Implicit illegal type handler. */
  implicit val handler: ValidationIllegalTypeHandler = ValidationIllegalTypeHandler(this)

  /** Report constraint failure of severity violation. */
  def violation(id: String, node: String, message: String, ast: YPart): Unit = {
    validation.reportConstraintFailure(VIOLATION, id, node, None, message, lexical(ast))
  }

  /** Report constraint failure of severity violation. */
  def violation(node: String, message: String, ast: YPart): Unit = {
    violation(ParsingErrorSpecification.id(), node, message, ast)
  }

  private def lexical(ast: YPart): Option[LexicalInformation] = {
    ast.range match {
      case InputRange.Zero => None
      case range           => Some(LexicalInformation(Range(range)))
    }
  }
}

case class ValidationIllegalTypeHandler(private val validation: ValidationAware) extends IllegalTypeHandler {

  override def handle[T](error: YError, defaultValue: T): T = {
    validation.violation("", error.error, part(error))
    defaultValue
  }

  private def part(error: YError): YPart = {
    error.node match {
      case d: YDocument => d
      case n: YNode     => n
      case s: YSuccess  => s.node
      case f: YFail     => part(f.error)
    }
  }
}
