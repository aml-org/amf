package amf.core.parser

import amf.core.annotations.{LexicalInformation, SourceLocation}
import amf.core.model.document.BaseUnit
import amf.core.model.domain.AmfObject
import amf.core.services.RuntimeValidator
import amf.core.utils.Strings
import amf.core.validation.SeverityLevels.{VIOLATION, WARNING}
import amf.core.validation.core.ValidationSpecification
import amf.core.{AMFCompilerRunCount, annotations}
import amf.plugins.features.validation.ParserSideValidations.{SyamlError, SyamlWarning}
import org.mulesoft.lexer.InputRange
import org.yaml.model._

import scala.collection.mutable

/**
  * Parser context
  */
trait RuntimeErrorHandler extends ErrorHandler {
  val parserCount: Int
  val currentFile: String

  def reportConstraint(id: String,
                       node: String,
                       property: Option[String],
                       message: String,
                       lexical: Option[LexicalInformation],
                       level: String,
                       location: Option[String]): Unit = {
    RuntimeValidator.reportConstraintFailure(level,
                                             id,
                                             node,
                                             property,
                                             message,
                                             lexical,
                                             parserCount,
                                             location.orElse(Some(currentFile)))
  }
}

trait ErrorHandler extends IllegalTypeHandler with ParseErrorHandler {

  override def handle[T](error: YError, defaultValue: T): T = {
    violation(SyamlError, "", error.error, part(error))
    defaultValue
  }

  def guiKey(message: String, location: Option[String], lexical: Option[LexicalInformation]) = {
    message ++ location.getOrElse("") ++ lexical.map(_.value).getOrElse("")
  }

  def reportConstraint(id: String,
                       node: String,
                       property: Option[String],
                       message: String,
                       lexical: Option[LexicalInformation],
                       level: String,
                       location: Option[String]): Unit

  /** Report constraint failure of severity violation. */
  def violation(specification: ValidationSpecification,
                node: String,
                property: Option[String],
                message: String,
                lexical: Option[LexicalInformation],
                location: Option[String]): Unit = {
    reportConstraint(specification.id, node, property, message, lexical, VIOLATION, location)
  }

  def violation(specification: ValidationSpecification,
                node: String,
                message: String,
                annotations: Annotations): Unit = {
    violation(specification,
              node,
              None,
              message,
              annotations.find(classOf[LexicalInformation]),
              annotations.find(classOf[SourceLocation]).map(_.location))
  }

  /** Report constraint failure of severity violation for the given amf object. */
  def violation(specification: ValidationSpecification,
                element: AmfObject,
                target: Option[String],
                message: String): Unit = {
    violation(specification, element.id, target, message, element.position(), element.location())
  }

  /** Report constraint failure of severity violation with location file. */
  def violation(specification: ValidationSpecification, node: String, message: String, location: String): Unit = {
    violation(specification, node, None, message, None, location.option)
  }

  /** Report constraint failure of severity violation. */
  def violation(specification: ValidationSpecification,
                node: String,
                property: Option[String],
                message: String,
                ast: YPart): Unit = {
    violation(specification, node, property, message, lexical(ast), ast.sourceName.option)
  }

  /** Report constraint failure of severity violation. */
  def violation(specification: ValidationSpecification, node: String, message: String, ast: YPart): Unit = {
    violation(specification, node, None, message, ast)
  }

  def violation(specification: ValidationSpecification, node: String, message: String): Unit = {
    violation(specification, node, None, message, None, None)
  }

  /** Report constraint failure of severity warning. */
  def warning(specification: ValidationSpecification,
              node: String,
              property: Option[String],
              message: String,
              lexical: Option[LexicalInformation],
              location: Option[String]): Unit = {
    reportConstraint(specification.id, node, property, message, lexical, WARNING, location)
  }

  /** Report constraint failure of severity violation for the given amf object. */
  def warning(specification: ValidationSpecification,
              element: AmfObject,
              target: Option[String],
              message: String): Unit = {
    warning(specification, element.id, target, message, element.position(), element.location())
  }

  /** Report constraint failure of severity warning. */
  def warning(specification: ValidationSpecification,
              node: String,
              property: Option[String],
              message: String,
              ast: YPart): Unit = {
    warning(specification, node, property, message, lexical(ast), ast.sourceName.option)
  }

  /** Report constraint failure of severity warning. */
  def warning(specification: ValidationSpecification, node: String, message: String, ast: YPart): Unit = {
    warning(specification, node, None, message, ast)
  }

  /** Report constraint failure of severity warning. */
  def warning(specification: ValidationSpecification, node: String, message: String, annotations: Annotations): Unit = {
    warning(specification,
            node,
            None,
            message,
            annotations.find(classOf[LexicalInformation]),
            annotations.find(classOf[SourceLocation]).map(_.location))
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
      case _                                                   => violation(SyamlError, "", e.getMessage, node)
    }
  }
}

object EmptyFutureDeclarations {
  def apply(): FutureDeclarations = new FutureDeclarations {}
}

case class ParserDefaultErrorHandler(override val parserCount: Int, override val currentFile: String)
    extends RuntimeErrorHandler

case class ParserContext(rootContextDocument: String = "",
                         refs: Seq[ParsedReference] = Seq.empty,
                         futureDeclarations: FutureDeclarations = EmptyFutureDeclarations(),
                         parserCount: Int = AMFCompilerRunCount.nextRun(),
                         eh: Option[ErrorHandler] = None)
    extends ErrorHandler {

  var globalSpace: mutable.Map[String, Any] = mutable.Map()
  var variables                             = ContextVariables()

  def forLocation(newLocation: String): ParserContext = {
    val copied: ParserContext = this.copy(rootContextDocument = newLocation)
    copied.reportDisambiguation = reportDisambiguation
    copied.globalSpace = globalSpace
    copied
  }

  private val sonsReferences: mutable.Map[String, BaseUnit] = mutable.Map()

  def addSonRef(ref: BaseUnit): this.type = this.synchronized {
    sonsReferences.get(ref.location().getOrElse(ref.id)) match {
      case Some(_) => // ignore
      case _ =>
        sonsReferences.put(ref.location().getOrElse(ref.id), ref)
    }
    this
  }

  private def getSonsParsedReferences: Seq[ParsedReference] =
    sonsReferences.values.map(u => ParsedReference(u, new Reference(u.location().getOrElse(u.id), Nil))).toSeq

  def copyWithSonsReferences(): ParserContext = {
    val context = this.copy(refs = this.refs ++ getSonsParsedReferences)
    context.reportDisambiguation = this.reportDisambiguation
    context.globalSpace = this.globalSpace
    context
  }

  var reportDisambiguation: mutable.Set[String] = mutable.Set()

  override def reportConstraint(id: String,
                                node: String,
                                property: Option[String],
                                message: String,
                                lexical: Option[LexicalInformation],
                                level: String,
                                location: Option[String]): Unit = {
    val reportKey = guiKey(message, location, lexical)
    if (!reportDisambiguation.contains(reportKey)) {
      reportDisambiguation += reportKey
      eh match {
        case Some(errorHandler) =>
          errorHandler.reportConstraint(id,
                                        node,
                                        property,
                                        message,
                                        lexical,
                                        level,
                                        location.orElse(Some(rootContextDocument)))
        case _ =>
          RuntimeValidator.reportConstraintFailure(level,
                                                   id,
                                                   node,
                                                   property,
                                                   message,
                                                   lexical,
                                                   parserCount,
                                                   location.orElse(Some(rootContextDocument)))
      }
    }
  }
}

case class WarningOnlyHandler(override val currentFile: String) extends RuntimeErrorHandler {
  override val parserCount: Int = AMFCompilerRunCount.count

  override def handle(node: YPart, e: SyamlException): Unit = {
    warning(SyamlWarning, "", e.getMessage, node)
    warningRegister = true
  }

  override def handle[T](error: YError, defaultValue: T): T = {
    warning(SyamlWarning, "", error.error, part(error))
    warningRegister = true
    defaultValue
  }

  private var warningRegister: Boolean = false

  def hasRegister: Boolean = warningRegister
}

object UnhandledErrorHandler extends ErrorHandler {
  override def handle(node: YPart, e: SyamlException): Unit = {
    throw new Exception(e.getMessage + " at: " + node.range, e)
  }

  override def reportConstraint(id: String,
                                node: String,
                                property: Option[String],
                                message: String,
                                lexical: Option[LexicalInformation],
                                level: String,
                                location: Option[String]): Unit = {
    throw new Exception(
      s"  Message: $message\n  Target: $node\nProperty: ${property.getOrElse("")}\n  Position: $lexical\n at location: $location")
  }
}

class DefaultParserSideErrorHandler(override val parserCount: Int, override val currentFile: String)
    extends RuntimeErrorHandler

object DefaultParserSideErrorHandler {
  def apply(model: BaseUnit): DefaultParserSideErrorHandler = {
    val parserCount: Int = {
      // this can get not set if the model has been created manually without parsing
      model.parserRun match {
        case Some(run) => run
        case None =>
          model.parserRun = Some(AMFCompilerRunCount.nextRun())
          model.parserRun.get
      }
    }

    new DefaultParserSideErrorHandler(parserCount, model.location().getOrElse(model.id))
  }
}
