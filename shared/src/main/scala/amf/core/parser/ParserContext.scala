package amf.core.parser

import amf.core.model.domain.LexicalInformation
import amf.core.remote.{Unknown, Vendor}
import amf.core.services.RuntimeValidator
import amf.core.validation.ParserSideValidations.ParsingErrorSpecification
import amf.core.validation.SeverityLevels.VIOLATION
import amf.plugins.document.webapi.contexts.SpecAwareContext
import amf.plugins.document.webapi.parser.spec.{Declarations, SpecSyntax}
import org.mulesoft.lexer.InputRange
import org.yaml.model._

/**
  * Parser context
  */
class ErrorHandler extends IllegalTypeHandler {

  override def handle[T](error: YError, defaultValue: T): T = {
    violation("", error.error, part(error))
    defaultValue
  }

  /** Report constraint failure of severity violation. */
  def violation(id: String,
                node: String,
                property: Option[String],
                message: String,
                lexical: Option[LexicalInformation]): Unit = {
    RuntimeValidator.reportConstraintFailure(VIOLATION, id, node, property, message, lexical)
  }

  def violation(message: String, ast: Option[YPart]): Unit = {
    violation("", "", None, message, ast.flatMap(lexical))
  }

  /** Report constraint failure of severity violation. */
  def violation(id: String, node: String, property: Option[String], message: String, ast: YPart): Unit = {
    violation(id, node, property, message, lexical(ast))
  }

  /** Report constraint failure of severity violation. */
  def violation(id: String, node: String, message: String, ast: YPart): Unit = {
    violation(id, node, None, message, ast)
  }

  /** Report constraint failure of severity violation. */
  def violation(node: String, message: String, ast: YPart): Unit = {
    violation(ParsingErrorSpecification.id(), node, message, ast)
  }

  private def part(error: YError): YPart = {
    error.node match {
      case d: YDocument => d
      case n: YNode     => n
      case s: YSuccess  => s.node
      case f: YFail     => part(f.error)
    }
  }

  private def lexical(ast: YPart): Option[LexicalInformation] = {
    ast.range match {
      case InputRange.Zero => None
      case range           => Some(LexicalInformation(Range(range)))
    }
  }
}

case class ParserContext(rootContextDocument: String = "", refs: Seq[ParsedReference] = Seq.empty, private val internalDec: Option[Declarations] = None)
    extends ErrorHandler {

  val declarations: Declarations = internalDec.getOrElse(Declarations(errorHandler = Some(this)))

  val vendor: Vendor = Unknown

  val syntax: SpecSyntax = NoneSyntax
  val spec: SpecAwareContext = NoneSpecContext

  object NoneSyntax extends SpecSyntax {
    override val nodes: Map[String, Set[String]] = Map()
  }

  object NoneSpecContext extends SpecAwareContext {
    override def link(node: YNode): Either[String, YNode] = Right(node)

    override def ignore(shape: String, property: String): Boolean = false
  }
}







