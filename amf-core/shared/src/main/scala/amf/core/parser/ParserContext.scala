package amf.core.parser

import amf.core.{AMFCompilerRunCount, annotations}
import amf.core.annotations.LexicalInformation
import amf.core.services.RuntimeValidator
import amf.core.validation.SeverityLevels.{VIOLATION, WARNING}
import amf.plugins.features.validation.ParserSideValidations.{ParsingErrorSpecification, ParsingWarningSpecification}
import org.mulesoft.lexer.InputRange
import org.yaml.model._

import scala.collection.mutable

/**
  * Parser context
  */
trait ErrorHandler extends IllegalTypeHandler with ParseErrorHandler {

  val parserCount: Int
  val currentFile: String

  override def handle[T](error: YError, defaultValue: T): T = {
    violation("", error.error, part(error))
    defaultValue
  }

  private def reportConstraint(id: String,
                               node: String,
                               property: Option[String],
                               message: String,
                               lexical: Option[LexicalInformation],
                               level: String): Unit = {
    RuntimeValidator.reportConstraintFailure(level, id, node, property, message, lexical, parserCount)
  }

  /** Report constraint failure of severity violation. */
  def violation(id: String,
                node: String,
                property: Option[String],
                message: String,
                lexical: Option[LexicalInformation]): Unit = {
    reportConstraint(id, node, property, message, lexical, VIOLATION)
  }

  /** Report constraint failure of severity violation. WITHOUT NODE ID. */
  def violation(message: String, ast: YPart): Unit = {
    violation(ParsingErrorSpecification.id(), "", None, message, lexical(ast))
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
    val errorLocation = if (node == "") currentFile else node
    violation(ParsingErrorSpecification.id(), errorLocation, message, ast)
  }

  /** Report constraint failure of severity violation. */
  def violation(node: String, message: String, lexical: Option[LexicalInformation]): Unit = {
    val errorLocation = if (node == "") currentFile else node
    violation(ParsingErrorSpecification.id(), errorLocation, None, message, lexical)
  }

  /** Report constraint failure of severity warning. */
  def warning(id: String,
              node: String,
              property: Option[String],
              message: String,
              lexical: Option[LexicalInformation]): Unit = {
    reportConstraint(id, node, property, message, lexical, WARNING)
  }

  /** Report constraint failure of severity warning. WITHOUT NODE ID. */
  def warning(message: String, ast: YPart): Unit = {
    warning(ParsingWarningSpecification.id(), "", None, message, lexical(ast))
  }

  /** Report constraint failure of severity warning. */
  def warning(id: String, node: String, property: Option[String], message: String, ast: YPart): Unit = {
    warning(id, node, property, message, lexical(ast))
  }

  /** Report constraint failure of severity warning. */
  def warning(id: String, node: String, message: String, ast: YPart): Unit = {
    warning(id, node, None, message, ast)
  }

  /** Report constraint failure of severity warning. */
  def warning(node: String, message: String, ast: YPart): Unit = {
    warning(ParsingWarningSpecification.id(), node, message, ast)
  }

  protected def part(error: YError): YPart = {
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
      case range           => Some(annotations.LexicalInformation(Range(range)))
    }
  }
  override def handle(node: YPart, e: SyamlException): Unit = {
    e match {
      // ignoring errors due to trailing white space
      case lexer: LexerException if lexer.text.matches("\\s+") => // ignore
      case _                                                   => violation("", e.getMessage, node)
    }
  }
}

object EmptyFutureDeclarations {
  def apply(): FutureDeclarations = new FutureDeclarations {}
}
case class ParserContext(rootContextDocument: String = "",
                         refs: Seq[ParsedReference] = Seq.empty,
                         futureDeclarations: FutureDeclarations = EmptyFutureDeclarations(),
                         parserCount: Int = AMFCompilerRunCount.nextRun())
    extends ErrorHandler {

  override val currentFile: String = rootContextDocument

  var globalSpace: mutable.Map[String, Any] = mutable.Map()
}

case class WarningOnlyHandler(override val currentFile: String) extends ErrorHandler {
  override val parserCount: Int = AMFCompilerRunCount.count

  override def handle(node: YPart, e: SyamlException): Unit = {
    warning("", e.getMessage, node)
    warningRegister = true
  }

  override def handle[T](error: YError, defaultValue: T): T = {
    warning("", error.error, part(error))
    warningRegister = true
    defaultValue
  }

  private var warningRegister: Boolean = false

  def hasRegister: Boolean = warningRegister
}
