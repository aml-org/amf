package amf.spec

import amf.domain.Annotation.LexicalInformation
import amf.parser.{Range, YMapOps, YValueOps}
import amf.remote.{Oas, Raml, Vendor}
import amf.spec.oas.OasSyntax
import amf.spec.raml.RamlSyntax
import amf.validation.SeverityLevels.VIOLATION
import amf.validation.Validation
import amf.validation.model.ParserSideValidations.{ClosedShapeSpecification, ParsingErrorSpecification}
import org.mulesoft.lexer.InputRange
import org.yaml.model._

/**
  * Parser context
  */
case class ParserContext(validation: Validation, vendor: Vendor) extends IllegalTypeHandler {

  def toOas: ParserContext = vendor match {
    case Oas => this
    case _   => copy(vendor = Oas)
  }

  def toRaml: ParserContext = vendor match {
    case Raml => this
    case _    => copy(vendor = Raml)
  }

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

  /** Validate closed shape. */
  def closedShape(node: String, ast: YMap, shape: String, annotation: Boolean = false): Unit = {
    syntax.nodes.get(shape) match {
      case Some(props) =>
        val properties = if (annotation) {
          props ++ syntax.nodes("annotation")
        } else {
          props
        }

        ast.entries.foreach { entry =>
          val key: String = entry.key
          if (spec.ignore(shape, key)) {
            // annotation or path in endpoint/webapi => ignore
          } else if (!properties(key)) {
            violation(ClosedShapeSpecification.id(),
                      node,
                      s"Property $key not supported in a $vendor $shape node",
                      entry)
          }
        }
      case None => throw new Exception(s"Cannot validate unknown node type $shape for $vendor")
    }
  }

  def link(value: YNode): Either[String, YNode] = spec.link(value)

  private def lexical(ast: YPart): Option[LexicalInformation] = {
    ast.range match {
      case InputRange.Zero => None
      case range           => Some(LexicalInformation(Range(range)))
    }
  }

  private def part(error: YError): YPart = {
    error.node match {
      case d: YDocument => d
      case n: YNode     => n
      case s: YSuccess  => s.node
      case f: YFail     => part(f.error)
    }
  }

  val syntax: SpecSyntax = vendor match {
    case Raml => RamlSyntax
    case Oas  => OasSyntax
    case _    => throw new Exception(s"Unsupported $vendor")
  }

  private val spec: SpecAwareContext = vendor match {
    case Raml => RamlSpecAwareContext
    case Oas  => OasSpecAwareContext
    case _    => throw new Exception(s"Unsupported $vendor")
  }
}

private trait SpecAwareContext {
  def link(node: YNode): Either[String, YNode]
  def ignore(shape: String, property: String): Boolean
}

private object RamlSpecAwareContext extends SpecAwareContext {

  override def link(node: YNode): Either[String, YNode] = {
    node match {
      case _ if isInclude(node) => Left(node.value.toScalar.text)
      case _                    => Right(node)
    }
  }

  override def ignore(shape: String, property: String): Boolean =
    (property.startsWith("(") && property.endsWith(")")) || (property.startsWith("/") && (shape == "webApi" || shape == "endPoint"))

  private def isInclude(node: YNode) = {
    node.tagType == YType.Unknown && node.tag.text == "!include"
  }
}

private object OasSpecAwareContext extends SpecAwareContext {

  override def link(node: YNode): Either[String, YNode] = {
    node match {
      case map if isMap(map) =>
        val ref: Option[String] = map.value.toMap.key("$ref").map(v => v.value)
        ref match {
          case Some(url) => Left(url)
          case None      => Right(node)
        }
      case _ => Right(node)
    }
  }

  override def ignore(shape: String, property: String): Boolean =
    property.startsWith("x-") || property == "$ref" || (property.startsWith("/") && shape == "webApi")

  private def isMap(node: YNode) = node.tag.tagType == YType.Map
}
