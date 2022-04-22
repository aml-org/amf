package amf.graphql.internal.spec.parser.syntax

import amf.antlr.client.scala.parse.syntax.AntlrASTParserHelper
import amf.core.client.scala.vocabulary.Namespace.XsdTypes
import amf.core.internal.parser.domain.SearchScope
import amf.graphql.internal.spec.context.GraphQLWebApiContext
import amf.graphql.internal.spec.parser.syntax.TokenTypes._
import amf.shapes.client.scala.model.domain._
import org.mulesoft.antlrast.ast.{ASTElement, Node, Terminal}
import org.mulesoft.lexer.SourceLocation

case class NullableShape(isNullable: Boolean, shape: AnyShape)

trait GraphQLASTParserHelper extends AntlrASTParserHelper {

  val INT          = "Int"
  val FLOAT        = "Float"
  val STRING       = "String"
  val BOOLEAN      = "Boolean"
  val ID           = "ID"
  val SCALAR_TYPES = Seq(INT, FLOAT, STRING, BOOLEAN, ID)

  def unpackNilUnion(shape: AnyShape): NullableShape = {
    shape match {
      case union: UnionShape if union.anyOf.length == 2 =>
        if (union.anyOf.head.isInstanceOf[NilShape]) {
          union.anyOf.find(!_.isInstanceOf[NilShape]) match {
            case Some(s: AnyShape) => NullableShape(isNullable = true, s)
            case _                 => NullableShape(isNullable = false, shape)
          }
        } else {
          NullableShape(isNullable = false, shape)
        }
      case _ => NullableShape(isNullable = false, shape)
    }
  }
  def findDescription(n: ASTElement): Option[Terminal] = {
    collect(n, Seq(DESCRIPTION, STRING_VALUE)).headOption.flatMap {
      case n: Node => n.children.collectFirst({ case t: Terminal => t })
      case _       => None
    }
  }

  def findName(n: Node, default: String, errorId: String, error: String)(implicit ctx: GraphQLWebApiContext): String = {
    val potentialPaths: Seq[Seq[String]] = Stream(
      Seq(NAME, NAME_TERMINAL),
      Seq(NAMED_TYPE, NAME, NAME_TERMINAL),
      Seq(NAME)
    )

    val maybeName = potentialPaths.map { path =>
      pathToTerminal(n, path)
    } collectFirst { case Some(t) => t.value }

    maybeName match {
      case Some(name) =>
        name
      case _ =>
        astError(errorId, error, toAnnotations(n))
        default
    }
  }

  def searchName(n: Node): Option[String] = pathToTerminal(n, Seq(NAME, NAME_TERMINAL)).map(_.value)

  // GraphQL specific helpers
  def parseType(n: Node, errorId: String)(implicit ctx: GraphQLWebApiContext): AnyShape = {
    path(n, Seq(TYPE_)) match {
      case Some(t: Node) =>
        if (isScalarType(t)) parseScalarType(t, errorId)
        else if (isListType(t)) parseListType(t, errorId)
        else if (isNamedType(t)) parseObjectType(t, errorId)
        else {
          astError(errorId, "Unknown input type syntax", toAnnotations(n))
          AnyShape(toAnnotations(n))
        }
      case _ =>
        astError(errorId, "Unknown input type syntax", toAnnotations(n))
        AnyShape(toAnnotations(n))
    }
  }

  def isScalarType(n: Node): Boolean = {
    path(n, Seq(NAMED_TYPE, NAME, NAME_TERMINAL)) match {
      case Some(nameTerminal: Terminal) =>
        val name = nameTerminal.value
        SCALAR_TYPES.contains(name)
      case _ =>
        false
    }
  }

  def isNamedType(n: Node): Boolean = {
    path(n, Seq(NAMED_TYPE, NAME, NAME_TERMINAL)) match {
      case Some(nameTerminal: Terminal) =>
        val name = nameTerminal.value
        !SCALAR_TYPES.contains(name)
      case _ =>
        false
    }
  }

  def isNullable(n: Node): Boolean = n.children.lastOption match {
    case Some(term: Terminal) if term.value == "!" => false
    case _                                         => true
  }

  def isListType(n: Node): Boolean = path(n, Seq(LIST_TYPE)).isDefined

  def parseScalarType(t: Node, errorId: String)(implicit ctx: GraphQLWebApiContext): AnyShape = {
    maybeNullable(
      t,
      errorId,
      (t, typeName) => {
        val scalar = ScalarShape(toAnnotations(t))
        typeName match {
          case INT     => scalar.withDataType(XsdTypes.xsdInteger.iri())
          case FLOAT   => scalar.withDataType(XsdTypes.xsdFloat.iri())
          case STRING  => scalar.withDataType(XsdTypes.xsdString.iri())
          case BOOLEAN => scalar.withDataType(XsdTypes.xsdBoolean.iri())
          case ID =>
            scalar.withDataType(XsdTypes.xsdString.iri())
            scalar.withFormat("ID")
          case _ =>
            astError(errorId, s"Unknown GraphQL scalar type $typeName", toAnnotations(t))
        }
        scalar
      }
    )
  }

  def parseListType(t: Node, errorId: String)(implicit ctx: GraphQLWebApiContext): AnyShape = {
    maybeNamedNullable(
      t,
      "",
      (t, _) => {
        val array = ArrayShape(toAnnotations(t))
        path(t, Seq(LIST_TYPE)) match {
          case Some(n: Node) =>
            val range = parseType(n, errorId)
            array.withItems(range)
          case _ =>
            astError(errorId, s"Unknown listType range", toAnnotations(t))
        }
        array
      }
    )
  }

  def parseObjectType(t: Node, errorId: String)(implicit ctx: GraphQLWebApiContext): AnyShape = {
    maybeNullable(t, errorId, (t, typeName) => {
      findOrLinkType(typeName, t)
    })
  }

  def findOrLinkType(typeName: String, t: ASTElement)(implicit ctx: GraphQLWebApiContext): AnyShape = {
    ctx.declarations.findType(typeName, SearchScope.All) match {
      case Some(s: ScalarShape) =>
        s.link(typeName, toAnnotations(t)).asInstanceOf[ScalarShape].withName(typeName, toAnnotations(t))
      case Some(s: NodeShape) =>
        s.link(typeName, toAnnotations(t)).asInstanceOf[NodeShape].withName(typeName, toAnnotations(t))
      case _ =>
        val shape = UnresolvedShape(typeName, toAnnotations(t))
        shape.withContext(ctx)
        shape.unresolved(typeName, Nil, Some(elementSourceLocation(t)))
        shape
    }
  }

  def maybeNullable(t: Node, errorId: String, parse: (Node, String) => AnyShape)(
      implicit ctx: GraphQLWebApiContext): AnyShape = {
    val typeName = findName(t, "UnknownType", errorId, "Cannot find type name")
    maybeNamedNullable(t, typeName, parse)
  }

  def maybeNamedNullable(t: Node, typeName: String, parse: (Node, String) => AnyShape)(
      implicit ctx: GraphQLWebApiContext): AnyShape = {
    val shape = parse(t, typeName)
    if (isNullable(t)) {
      UnionShape()
        .withId(shape.id + "/nullwrapper")
        .withAnyOf(
          Seq(
            NilShape().withId(shape.id + "/nullwrapper/nil"),
            shape
          ))
    } else {
      shape
    }
  }

  def cleanDocumentation(doc: String): String = {
    doc.replaceAll("\"\"\"", "").trim
  }

  def elementSourceLocation(t: ASTElement): SourceLocation =
    new SourceLocation(t.file, 0, 0, t.start.line, t.start.column, t.end.line, t.end.column)
}
