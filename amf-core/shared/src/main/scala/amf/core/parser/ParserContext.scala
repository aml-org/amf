package amf.core.parser

import amf.core.{AMFCompilerRunCount, annotations}
import amf.core.annotations.{LexicalInformation, SourceLocation}
import amf.core.model.document.BaseUnit
import amf.core.model.domain.AmfObject
import amf.core.services.RuntimeValidator
import amf.core.validation.SeverityLevels.{VIOLATION, WARNING}
import amf.plugins.features.validation.ParserSideValidations.{ParsingErrorSpecification, ParsingWarningSpecification}
import org.mulesoft.lexer.InputRange
import org.yaml.model._
import amf.core.utils.Strings

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
    violation("", error.error, part(error))
    defaultValue
  }

  def reportConstraint(id: String,
                       node: String,
                       property: Option[String],
                       message: String,
                       lexical: Option[LexicalInformation],
                       level: String,
                       location: Option[String]): Unit

  /** Report constraint failure of severity violation. */
  def violation(id: String,
                node: String,
                property: Option[String],
                message: String,
                lexical: Option[LexicalInformation],
                location: Option[String]): Unit = {
    reportConstraint(id, node, property, message, lexical, VIOLATION, location)
  }

  def violation(id: String, node: String, message: String, annotations: Annotations): Unit = {
    violation(id,
              node,
              None,
              message,
              annotations.find(classOf[LexicalInformation]),
              annotations.find(classOf[SourceLocation]).map(_.location))
  }

  /** Report constraint failure of severity violation for the given amf object. */
  def violation(id: String, element: AmfObject, target: Option[String], message: String): Unit = {
    reportConstraint(id, element.id, target, message, element.position(), VIOLATION, element.location())
  }

  /** Report constraint failure of severity violation. WITHOUT NODE ID. */
  def violation(message: String, ast: YPart): Unit = {
    violation(ParsingErrorSpecification.id, "", None, message, lexical(ast), ast.sourceName.option)
  }

  /** Report constraint failure of severity violation. */
  def violation(id: String, node: String, property: Option[String], message: String, ast: YPart): Unit = {
    violation(id, node, property, message, lexical(ast), ast.sourceName.option)
  }

  /** Report constraint failure of severity violation. */
  def violation(id: String, node: String, message: String, ast: YPart): Unit = {
    violation(id, node, None, message, ast)
  }

  /** Report constraint failure of severity violation. */
  def violation(node: String, message: String, ast: YPart): Unit = {
    violation(ParsingErrorSpecification.id, node, message, ast)
  }

  /** Report constraint failure of severity violation with location file. */
  def violation(node: String, message: String, location: String): Unit = {
    violation(ParsingErrorSpecification.id, node, None, message, None, location.option)
  }

  /** Report constraint failure of severity violation. */
  def violation(node: String, message: String, lexical: Option[LexicalInformation], location: Option[String]): Unit = {
    violation(ParsingErrorSpecification.id, node, None, message, lexical, location)
  }

  /** Report constraint failure of severity warning. */
  def warning(id: String,
              node: String,
              property: Option[String],
              message: String,
              lexical: Option[LexicalInformation],
              location: Option[String]): Unit = {
    reportConstraint(id, node, property, message, lexical, WARNING, location)
  }

  /** Report constraint failure of severity violation for the given amf object. */
  def warning(id: String, element: AmfObject, target: Option[String], message: String): Unit = {
    reportConstraint(id, element.id, target, message, element.position(), WARNING, element.location())
  }

  /** Report constraint failure of severity warning. WITHOUT NODE ID. */
  def warning(message: String, ast: YPart): Unit = {
    warning(ParsingWarningSpecification.id, "", None, message, lexical(ast), ast.sourceName.option)
  }

  /** Report constraint failure of severity warning. */
  def warning(id: String, node: String, property: Option[String], message: String, ast: YPart): Unit = {
    warning(id, node, property, message, lexical(ast), ast.sourceName.option)
  }

  /** Report constraint failure of severity warning. */
  def warning(id: String, node: String, message: String, ast: YPart): Unit = {
    warning(id, node, None, message, ast)
  }

  /** Report constraint failure of severity warning. */
  def warning(node: String, message: String, ast: YPart): Unit = {
    warning(ParsingWarningSpecification.id, node, message, ast)
  }

  /** Report constraint failure of severity warning. */
  def warning(id: String, node: String, message: String, annotations: Annotations): Unit = {
    reportConstraint(id,
                     node,
                     None,
                     message,
                     annotations.find(classOf[LexicalInformation]),
                     WARNING,
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
    extends RuntimeErrorHandler {

  override val currentFile: String = rootContextDocument

  var globalSpace: mutable.Map[String, Any] = mutable.Map()

  def violation(node: String, message: String): Unit = violation(node, message, currentFile)

  def forLocation(newLocation: String): ParserContext = {
    val copied: ParserContext = this.copy(rootContextDocument = newLocation)
    copied.globalSpace = globalSpace
    copied
  }

  private val sonsReferences: mutable.Map[String, BaseUnit] = mutable.Map()

  def addSonRef(ref: BaseUnit): this.type = this.synchronized {
    sonsReferences.get(ref.location().getOrElse(ref.id)) match {
      case Some(u) => // ignore
      case _ =>
        sonsReferences.put(ref.location().getOrElse(ref.id), ref)
    }
    this
  }

  private def getSonsParsedReferences: Seq[ParsedReference] =
    sonsReferences.values.map(u => ParsedReference(u, new Reference(u.location().getOrElse(u.id), Nil))).toSeq

  def copyWithSonsReferences(): ParserContext = {
    val context = this.copy(refs = this.refs ++ getSonsParsedReferences)
    context.globalSpace = this.globalSpace
    context
  }
}

case class WarningOnlyHandler(override val currentFile: String) extends RuntimeErrorHandler {
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
