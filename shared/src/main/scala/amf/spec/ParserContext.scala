package amf.spec

import amf.compiler.ParsedReference
import amf.framework.domain.LexicalInformation
import amf.framework.validation.SeverityLevels.VIOLATION
import amf.parser.Range
import amf.plugins.document.webapi.contexts.{OasSpecAwareContext, RamlSpecAwareContext, SpecAwareContext, WebApiContext}
import amf.remote.{Oas, Raml, Unknown, Vendor}
import amf.shape.Shape
import amf.spec.oas.OasSyntax
import amf.spec.raml.RamlSyntax
import amf.validation.Validation
import amf.validation.model.ParserSideValidations.{ParsingErrorSpecification,ClosedShapeSpecification}
import org.mulesoft.lexer.InputRange
import org.yaml.model._

/**
  * Parser context
  */
class ErrorHandler(validation: Validation) extends IllegalTypeHandler {

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
    validation.reportConstraintFailure(VIOLATION, id, node, property, message, lexical)
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

case class ParserContext(validation: Validation, rootContextDocument: String = "", refs: Seq[ParsedReference] = Seq.empty, private val internalDec: Option[Declarations] = None)
    extends ErrorHandler(validation) {

  val declarations: Declarations = internalDec.getOrElse(Declarations(errorHandler = Some(this)))

  def toOas: WebApiContext = new WebApiContext(Oas, this, OasSpecAwareContext, OasSyntax)

  /**
    * raml types nodes are different from other shapes because they can have 'custom facets' essentially, client
    * defined constraints expressed as additional properties syntactically in the type definition.
    * The problem is that they cannot be recognised just looking into the AST as we do with annotations, so we
    * need to first, compute them, and then, add them as additional valid properties to the set of properties that
    * can be defined in the AST node
    */
  def closedRamlTypeShape(shape: Shape, ast: YMap, shapeType: String, annotation: Boolean = false): Unit = {
    val node   = shape.id
    val facets = shape.collectCustomShapePropertyDefinitions(onlyInherited = true)

    syntax.nodes.get(shapeType) match {
      case Some(props) =>
        val initialProperties = if (annotation) {
          props ++ syntax.nodes("annotation")
        } else {
          props
        }
        val allResults: Seq[Seq[YMapEntry]] = facets.map { propertiesMap =>
          val totalProperties     = initialProperties ++ propertiesMap.keys.toSet
          val acc: Seq[YMapEntry] = Seq.empty
          ast.entries.foldLeft(acc) { (results: Seq[YMapEntry], entry) =>
            val key: String = entry.key
            if (spec.ignore(shapeType, key)) {
              results
            } else if (!totalProperties(key)) {
              results ++ Seq(entry)
            } else {
              results
            }
          }
        }
        allResults.find(_.nonEmpty) match {
          case None => // at least we found a solution, this is a valid shape
          case Some(errors: Seq[YMapEntry]) =>
            violation(ClosedShapeSpecification.id(),
                      node,
                      s"Properties ${errors.map(_.key).mkString(",")} not supported in a $vendor $shapeType node",
                      errors.head) // pointing only to the first failed error
        }

      case None => throw new Exception(s"Cannot validate unknown node type $shapeType for $vendor")
    }
  }

  def toRaml: WebApiContext = new WebApiContext(Raml, this, RamlSpecAwareContext, RamlSyntax)


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







