package amf.graphql.internal.spec.parser.syntax

import amf.antlr.client.scala.parse.syntax.AntlrASTParserHelper
import amf.core.client.scala.model.DataType
import amf.core.client.scala.model.domain.{AmfScalar, DomainElement, Shape}
import amf.core.internal.metamodel.domain.common.DescriptionField
import amf.core.internal.parser.domain.Annotations.{inferred, synthesized, virtual}
import amf.core.internal.parser.domain.{Annotations, SearchScope}
import amf.graphql.internal.spec.context.{GraphQLBaseWebApiContext, GraphQLWebApiContext}
import amf.graphql.internal.spec.parser.syntax.ValueParser.parseValue
import amf.graphql.internal.spec.parser.syntax.TokenTypes._
import amf.graphqlfederation.internal.spec.context.GraphQLFederationWebApiContext
import amf.shapes.client.scala.model.domain._
import amf.shapes.client.scala.model.domain.operations.AbstractParameter
import org.mulesoft.antlrast.ast.{ASTNode, Node, Terminal}

import scala.reflect.ClassTag

case class NullableShape(isNullable: Boolean, shape: AnyShape)

trait GraphQLASTParserHelper extends AntlrASTParserHelper {
  def unpackNilUnion(shape: AnyShape): NullableShape = {
    shape match {
      case union: UnionShape if isNilUnion(union) =>
        findNonNilComponent(union) match {
          case Some(s: AnyShape) => NullableShape(isNullable = true, s)
          case _                 => NullableShape(isNullable = false, shape) // unreachable code
        }
      case _ => NullableShape(isNullable = false, shape)
    }
  }

  private def findNonNilComponent(union: UnionShape) = union.anyOf.find(!_.isInstanceOf[NilShape])

  private def isNilUnion(union: UnionShape) =
    union.anyOf.length == 2 && union.anyOf.exists(_.isInstanceOf[NilShape]) && union.anyOf.exists(x =>
      !x.isInstanceOf[NilShape]
    )

  def parseDescription(n: ASTNode, element: DomainElement, model: DescriptionField): Unit = {
    findDescription(n).foreach { desc =>
      element.set(model.Description, cleanDocumentation(desc.value), toAnnotations(desc))
    }
  }

  def parseDescription(n: ASTNode): Option[AmfScalar] = {
    findDescription(n).map { desc => AmfScalar(cleanDocumentation(desc.value), toAnnotations(desc)) }
  }

  def findDescription(n: ASTNode): Option[Terminal] = {
    collect(n, Seq(DESCRIPTION, STRING_VALUE)).headOption.flatMap {
      case n: Node => n.children.collectFirst({ case t: Terminal => t })
      case _       => None
    }
  }

  def findName(n: Node, default: String, error: String)(implicit
      ctx: GraphQLBaseWebApiContext
  ): (String, Annotations) = {
    val potentialPaths: Seq[Seq[String]] = Stream(
      Seq(NAME, NAME_TERMINAL),
      Seq(NAMED_TYPE, NAME, NAME_TERMINAL),
      Seq(NAME, KEYWORD),
      Seq(NAME),
      Seq(NAME_F, NAME_TERMINAL_F),
      Seq(NAME_F, KEYWORD_F),
      Seq(NAME_F)
    )

    val effectivePath = potentialPaths.map(path(n, _)) collectFirst {
      case Some(t: Terminal) => t
      case Some(n: Node) if n.children.size == 1 && n.children.head.isInstanceOf[Terminal] =>
        n.children.head.asInstanceOf[Terminal]
    }
    val maybeName = effectivePath.map(_.value)

    maybeName match {
      case Some(name) =>
        (name, toAnnotations(effectivePath.get))
      case _ =>
        astError(error, toAnnotations(n))
        (default, Annotations())
    }
  }

  def searchName(n: Node): Option[String] = pathToTerminal(n, Seq(NAME, NAME_TERMINAL)).map(_.value)

  // GraphQL specific helpers
  def parseType(n: Node)(implicit ctx: GraphQLBaseWebApiContext): AnyShape = {
    path(n, Seq(TYPE_)) match {
      case Some(t: Node) =>
        val shape = {
          if (isScalarType(t)) parseScalarType(t)
          else if (isListType(t)) parseListType(t)
          else if (isNamedType(t)) parseObjectType(t)
          else {
            astError("Unknown input type syntax", toAnnotations(n))
            AnyShape(toAnnotations(n))
          }
        }
        shape
      case _ =>
        astError("Unknown input type syntax", toAnnotations(n))
        val shape = AnyShape(toAnnotations(n))
        shape
    }
  }

  def isScalarType(n: Node): Boolean = getTypeName(n).exists(SCALAR_TYPES.contains)

  def isEnumType(n: Node)(implicit ctx: GraphQLBaseWebApiContext): Boolean = getTypeName(n).exists { name =>
    ctx.declarations.findType(name, SearchScope.All).exists(_.isInstanceOf[ScalarShape])
  }

  def getTypeName(n: Node): Option[String] = getTypeTerminal(n).map(_.value)

  def isNamedType(n: Node): Boolean = getTypeName(n).exists(!SCALAR_TYPES.contains(_))

  def getTypeTerminal(n: Node): Option[Terminal] =
    path(n, Seq(NAMED_TYPE, NAME, NAME_TERMINAL)) match {
      case Some(nameTerminal: Terminal) =>
        Some(nameTerminal)
      case _ => None
    }

  def isNullable(n: Node): Boolean = n.children.lastOption match {
    case Some(term: Terminal) if term.value == "!" => false
    case _                                         => true
  }

  def isListType(n: Node): Boolean = path(n, Seq(LIST_TYPE)).isDefined

  def parseScalarType(t: Node)(implicit
      ctx: GraphQLBaseWebApiContext
  ): AnyShape = {
    val parseFn: (Node, String) => AnyShape = (t, typeName) => {
      val scalar = ScalarShape(toAnnotations(t))
      typeName match {
        case INT     => scalar.withDataType(DataType.Integer)
        case FLOAT   => scalar.withDataType(DataType.Float)
        case STRING  => scalar.withDataType(DataType.String)
        case BOOLEAN => scalar.withDataType(DataType.Boolean)
        case ID =>
          scalar.withDataType(DataType.Any)
          scalar.withFormat("ID")
        case _ =>
          astError(s"Unknown GraphQL scalar type $typeName", toAnnotations(t))
      }
      scalar
    }
    maybeNullable(t, parseFn)
  }

  def parseListType(t: Node)(implicit
      ctx: GraphQLBaseWebApiContext
  ): AnyShape = {
    val parseFn: (Node, String) => AnyShape = (t, _) => {
      val array = ArrayShape(toAnnotations(t))
      path(t, Seq(LIST_TYPE)) match {
        case Some(n: Node) =>
          val range = parseType(n)
          array.withItems(range)
        case _ =>
          astError(s"Unknown listType range", toAnnotations(t))
      }
      array
    }
    maybeNamedNullable(t, "", parseFn)
  }

  def parseObjectType(t: Node)(implicit
      ctx: GraphQLBaseWebApiContext
  ): AnyShape = {
    val parseFn: (Node, String) => AnyShape = (t, typeName) => {
      findOrLinkType(typeName, t)
    }
    maybeNullable(t, parseFn)
  }

  def findOrLinkType(typeName: String, t: ASTNode)(implicit ctx: GraphQLBaseWebApiContext): AnyShape = {
    ctx.declarations.findType(typeName, SearchScope.All) match {
      case Some(s: ScalarShape) =>
        s.link(typeName, toAnnotations(t)).asInstanceOf[ScalarShape].withName(typeName, toAnnotations(t))
      case Some(s: NodeShape) =>
        s.link(typeName, toAnnotations(t)).asInstanceOf[NodeShape].withName(typeName, toAnnotations(t))
      case Some(s: UnionShape) =>
        s.link(typeName, toAnnotations(t)).asInstanceOf[UnionShape].withName(typeName, toAnnotations(t))
      case _ =>
        unresolvedShape(typeName, t)
    }
  }

  def maybeNullable(t: Node, parse: (Node, String) => AnyShape)(implicit
      ctx: GraphQLBaseWebApiContext
  ): AnyShape = {
    val (typeName, _) = findName(t, "UnknownType", "Cannot find type name")
    maybeNamedNullable(t, typeName, parse)
  }

  def maybeNamedNullable(t: Node, typeName: String, parse: (Node, String) => AnyShape)(implicit
      ctx: GraphQLBaseWebApiContext
  ): AnyShape = {
    val shape = parse(t, typeName)
    if (isNullable(t)) {
      UnionShape()
        .withAnyOf(
          Seq(
            NilShape(virtual()),
            shape
          ),
          synthesized()
        )
    } else {
      shape
    }
  }

  def cleanDocumentation(doc: String): String = {
    val trimmed = doc.replaceAll("\"\"\"", "").trim
    if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) trimmed.substring(1, trimmed.length - 1)
    else trimmed
  }

  def trimQuotes(value: String): String = {
    if (value.startsWith("\"") && value.endsWith("\"")) value.substring(1, value.length - 1)
    else value
  }

  def unresolvedShape(typeName: String, element: ASTNode)(implicit ctx: GraphQLBaseWebApiContext): UnresolvedShape = {
    val shape = UnresolvedShape(typeName, toAnnotations(element))
    shape.withContext(ctx)
    shape.unresolved(typeName, Nil, Some(element.location))
    shape
  }

  def contextually[T <: GraphQLBaseWebApiContext](fn: (T) => Any)(implicit
      ctx: GraphQLBaseWebApiContext,
      ct: ClassTag[T]
  ): Unit = {
    ctx match {
      case c: T => fn(c)
      case _    => // nothing
    }
  }

  def inFederation(fn: (GraphQLFederationWebApiContext) => Any)(implicit ctx: GraphQLBaseWebApiContext): Unit =
    contextually[GraphQLFederationWebApiContext](fn)

  def inGraphQL(fn: (GraphQLWebApiContext) => Any)(implicit ctx: GraphQLBaseWebApiContext): Unit =
    contextually[GraphQLWebApiContext](fn)

  def setDefaultValue(n: Node, parameter: AbstractParameter)(implicit ctx: GraphQLBaseWebApiContext): Unit = {
    parseValue(n, Seq(DEFAULT_VALUE, VALUE)).map(value => parameter.withDefaultValue(value))
  }

  def setDefaultValue(n: Node, shape: Shape)(implicit ctx: GraphQLBaseWebApiContext): Unit =
    parseValue(n, Seq(DEFAULT_VALUE, VALUE)).map(value => shape.withDefault(value, inferred()))
}
