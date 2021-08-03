package amf.graphql.internal.spec.parser.syntax

import amf.antlr.client.scala.parse.syntax.AntlrASTParserHelper
import amf.core.client.scala.vocabulary.Namespace.XsdTypes
import amf.core.internal.parser.domain.SearchScope
import amf.graphql.internal.spec.context.GraphQLWebApiContext
import amf.graphql.internal.spec.parser.syntax.TokenTypes._
import amf.shapes.client.scala.model.domain.{AnyShape, ArrayShape, NodeShape, ScalarShape, UnresolvedShape}
import org.mulesoft.antlrast.ast.{ASTElement, Node, Terminal}
import org.mulesoft.lexer.SourceLocation

trait GraphQLASTParserHelper extends AntlrASTParserHelper {

  implicit val ctx: GraphQLWebApiContext

  val INT = "Int"
  val FLOAT = "Float"
  val STRING = "String"
  val BOOLEAN = "Boolean"
  val ID = "ID"
  val SCALAR_TYPES = Seq(INT, FLOAT, STRING, BOOLEAN, ID)

  def findDescription(n: ASTElement): Option[Terminal] = {
    collect(n, Seq(DESCRIPTION, STRING_VALUE)).headOption match {
      case Some(t: Terminal) => Some(t)
      case _                 => None
    }
  }

  def findName(n: Node, default: String, errorId: String, error: String): String = {
    find(n, NAME).headOption match {
      case Some(t: Terminal) => t.value
      case _                 =>
        astError(errorId, error, toAnnotations(n))
        default
    }
  }

  // GraphQL specific helpers
  def parseType(n: Node, errorId: String): AnyShape = {
    path(n, Seq(TYPE_)) match {
      case Some(t: Node) =>
        if (isScalarType(t)) parseScalarType(t, errorId)
        else if (isListType(t)) parseListType(t, errorId)
        else if (isNamedType(t)) parseObjectType(t, errorId)
        else {
          astError(errorId, "Unknwn input type syntax", toAnnotations(n))
          AnyShape(toAnnotations(n))
        }
      case _       =>
        astError(errorId, "Unknwn input type syntax", toAnnotations(n))
        AnyShape(toAnnotations(n))
    }
  }
  def isScalarType(n: Node): Boolean = {
    path(n, Seq(NAMED_TYPE, NAME)) match {
      case Some(nameTerminal: Terminal) =>
        val name = nameTerminal.value
        SCALAR_TYPES.contains(name)
      case _                            =>
        false
    }
  }

  def isNamedType(n: Node): Boolean = {
    path(n, Seq(NAMED_TYPE, NAME)) match {
      case Some(nameTerminal: Terminal) =>
        val name = nameTerminal.value
        !SCALAR_TYPES.contains(name)
      case _                            =>
        false
    }
  }

  def isListType(n: Node): Boolean = path(n, Seq(LIST_TYPE)).isDefined


  def parseScalarType(t: Node, errorId: String): ScalarShape = {
    val scalar = ScalarShape(toAnnotations(t))
    path(t, Seq(NAMED_TYPE, NAME)) match {
      case Some(t: Terminal) =>
        t.value match {
          case INT => scalar.withDataType(XsdTypes.xsdInteger.iri())
          case FLOAT => scalar.withDataType(XsdTypes.xsdFloat.iri())
          case STRING => scalar.withDataType(XsdTypes.xsdString.iri())
          case BOOLEAN => scalar.withDataType(XsdTypes.xsdBoolean.iri())
          case ID      =>
            scalar.withDataType(XsdTypes.xsdString.iri())
            scalar.withFormat("ID")
          case _       =>
            astError(errorId, s"Unknown GraphQL scalar type ${t.value}", toAnnotations(t))
        }
    }
    scalar
  }

  def parseListType(t: Node, errorId: String): ArrayShape = {
    val array = ArrayShape(toAnnotations(t))
    path(t, Seq(LIST_TYPE, TYPE_)) match {
      case Some(n: Node) =>
        val range = parseType(n, errorId)
        array.withItems(range)
      case _             =>
        astError(errorId, s"Unknown listType range", toAnnotations(t))
    }
    array
  }

  def parseObjectType(t: Node, errorId: String): AnyShape = {
    val typeName = findName(t, "UnknownType", errorId, "Cannot find tpe name")
    ctx.declarations.findType(typeName, SearchScope.All) match {
      case Some(s: NodeShape) =>
        s.link(typeName, toAnnotations(t)).asInstanceOf[NodeShape].withName(typeName, toAnnotations(t))
      case _ =>
        val shape = UnresolvedShape(typeName, toAnnotations(t))
        shape.withContext(ctx)
        shape.unresolved(typeName, Nil, Some(new SourceLocation(t.file, 0, 0, t.start.line, t.start.column, t.end.line, t.end.column)))
        shape
    }
  }

}
