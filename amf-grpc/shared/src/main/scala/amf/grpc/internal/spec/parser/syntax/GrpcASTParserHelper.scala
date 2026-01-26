package amf.grpc.internal.spec.parser.syntax

import amf.antlr.client.scala.parse.syntax.AntlrASTParserHelper
import amf.apicontract.internal.validation.definitions.ParserSideValidations.{DuplicatedEnum, DuplicatedMessage}
import amf.core.client.scala.model.domain.extensions.DomainExtension
import amf.core.client.scala.model.domain.{AmfScalar, NamedDomainElement, Shape}
import amf.core.internal.annotations.DeclaredElement
import amf.core.internal.metamodel.domain.ShapeModel
import amf.core.internal.parser.domain.SearchScope
import amf.grpc.internal.spec.parser.context.GrpcWebApiContext
import amf.grpc.internal.spec.parser.domain.GrpcOptionParser
import amf.grpc.internal.spec.parser.syntax.TokenTypes._
import amf.shapes.client.scala.model.domain._
import amf.shapes.internal.domain.metamodel.{ArrayShapeModel, ScalarShapeModel}
import amf.shapes.internal.domain.parser.XsdTypeDefMapping
import amf.shapes.internal.spec.common.TypeDef
import amf.shapes.internal.spec.common.TypeDef._
import org.mulesoft.antlrast.ast.{ASTNode, Node, Terminal}

trait GrpcASTParserHelper extends AntlrASTParserHelper {

  def withName(ast: Node, nameToken: String, element: NamedDomainElement)(implicit
      ctx: GrpcWebApiContext
  ): Unit = {
    path(ast, Seq(nameToken, IDENTIFIER)).foreach { node =>
      val ann = toAnnotations(ast)
      withOptTerminal(node) {
        case Some(shapeName) =>
          element.withName(shapeName.value, ann)
        case None =>
          path(node, Seq(KEYWORDS)) match {
            case Some(keywordNode) =>
              withOptTerminal(keywordNode) {
                case Some(kw) =>
                  element.withName(kw.value, ann)
                case _ => astError(s"missing Protobuf3 $nameToken", element.annotations)
              }
            case None => astError(s"missing Protobuf3 $nameToken", element.annotations)
          }
      }
    }
  }

  def withDeclaredShape(ast: Node, nameToken: String, element: AnyShape)(implicit
      ctx: GrpcWebApiContext
  ): Unit = {
    path(ast, Seq(nameToken, IDENTIFIER)).foreach { node =>
      val ann = toAnnotations(node)
      withOptTerminal(node) {
        case Some(shapeName) =>
          element.withName(ctx.fullMessagePath(shapeName.value), ann)
          element set (shapeName.value, ann) as ShapeModel.DisplayName
          // Validation for duplicated Message/Enum names
          val name = element.name.value()
          findType(name) match {
            case Some(_) =>
              element match {
                case _: ScalarShape =>
                  ctx.eh.violation(DuplicatedEnum, element, s"Duplicated Enum name $name", element.annotations)
                case _ =>
                  ctx.eh.violation(DuplicatedMessage, element, s"Duplicated Message name $name", element.annotations)
              }
            case None => // ignore
          }
          ctx.declarations += element
          element.add(DeclaredElement())
        case None =>
          astError(s"missing Protobuf3 $nameToken", element.annotations)
      }
    }

  }

  def parseFieldNumber(ast: ASTNode)(implicit ctx: GrpcWebApiContext): Option[Int] = {
    path(ast, Seq(FIELD_NUMBER, INT_LITERAL)) match {
      case Some(n: ASTNode) =>
        withOptTerminal(n) {
          case Some(t) =>
            val order = Integer.parseInt(t.value)
            Some(order)
          case _ =>
            None
        }
      case _ =>
        None
    }
  }

  def parseFieldRange(ast: ASTNode, field: String = FIELD_TYPE)(implicit ctx: GrpcWebApiContext): Option[Shape] = {
    path(ast, Seq(field)) match {
      case Some(n: ASTNode) =>
        // scalar or object range
        val shape: Option[Shape] = withOptTerminal(n) {
          case Some(t) =>
            t.value match {
              case "double"   => Some(parseScalarRange(n, DoubleType, None))
              case "float"    => Some(parseScalarRange(n, FloatType, None))
              case "int32"    => Some(parseScalarRange(n, IntType, Some("int32")))
              case "int64"    => Some(parseScalarRange(n, LongType, Some("int64")))
              case "uint32"   => Some(parseScalarRange(n, IntType, Some("uint32")))
              case "uint64"   => Some(parseScalarRange(n, LongType, Some("uint64")))
              case "sint32"   => Some(parseScalarRange(n, IntType, Some("sint32")))
              case "sint64"   => Some(parseScalarRange(n, LongType, Some("sint64")))
              case "fixed32"  => Some(parseScalarRange(n, IntType, Some("fixed32")))
              case "fixed64"  => Some(parseScalarRange(n, LongType, Some("fixed64")))
              case "sfixed32" => Some(parseScalarRange(n, IntType, Some("sfixed32")))
              case "sfixed64" => Some(parseScalarRange(n, LongType, Some("sfixed64")))
              case "bool"     => Some(parseScalarRange(n, BoolType, None))
              case "string"   => Some(parseScalarRange(n, StrType, None))
              case "bytes"    => Some(parseScalarRange(n, ByteType, None))
              case _          => None
            }
          case _ =>
            path(n, Seq(MESSAGE_TYPE)).flatMap { case messageRef: Node =>
              Some(parseObjectRange(n, messageRef.source))
            }
        }
        // check if array
        shape map { shape =>
          if (parseIsRepeated(ast)) {
            ArrayShape(toAnnotations(ast)) set shape as ArrayShapeModel.Items
          } else shape
        }
      case _ => None
    }
  }

  protected def parseObjectRange(n: ASTNode, literalReference: String)(implicit ctx: GrpcWebApiContext): AnyShape = {
    val ann                = toAnnotations(n)
    val topLevelAlias      = ctx.topLevelPackageRef(literalReference).map(alias => Seq(alias)).getOrElse(Nil)
    val qualifiedReference = ctx.fullMessagePath(literalReference)
    val externalReference =
      s".$literalReference" // absolute reference based on the assumption the reference is for an external package imported in the file
    ctx.declarations
      .findType(
        qualifiedReference,
        SearchScope.All
      )         // local reference inside a nested message, transformed into a top-level for possibly nested type
      .orElse { // top-level reference for a reference, using just the name + plus package
        topLevelAlias.headOption.flatMap { alias =>
          ctx.declarations.findType(alias, SearchScope.All)
        }
      }
      .orElse(
        ctx.globalSpace.get(externalReference)
      ) // fully qualified reference for an external package that might be in the global space
      .orElse { // fully qualified reference for this package that might have been defined in a different file, and thus might be registered in the global space
        topLevelAlias.headOption.flatMap { alias =>
          ctx.globalSpace.get(alias)
        }
      } match {
      case Some(s: NodeShape) =>
        s.link(literalReference, ann).asInstanceOf[NodeShape].withName(literalReference, ann)
      case Some(s: ScalarShape) =>
        s.link(literalReference, ann)
          .asInstanceOf[ScalarShape]
          .withName(literalReference, ann)
      case Some(s: AnyShape) =>
        s.link(literalReference, ann)
          .asInstanceOf[AnyShape]
          .withName(literalReference, ann)
      case _ =>
        val shape = UnresolvedShape(literalReference, ann)
        shape.withContext(ctx)
        shape.unresolved(literalReference, Seq(qualifiedReference) ++ topLevelAlias, Some(n.location))
        shape
    }
  }

  protected def findType(name: String)(implicit ctx: GrpcWebApiContext): Option[AnyShape] = {
    ctx.declarations.findType(name, SearchScope.All)
  }

  private def parseScalarRange(n: ASTNode, scalarType: TypeDef, format: Option[String]): ScalarShape = {
    val ann      = toAnnotations(n)
    val scalar   = ScalarShape(ann)
    val datatype = XsdTypeDefMapping.xsd(scalarType)
    scalar set AmfScalar(datatype, ann) as ScalarShapeModel.DataType
    format.map(format => scalar set AmfScalar(format, ann) as ScalarShapeModel.Format)
    scalar
  }

  private def parseIsRepeated(ast: ASTNode): Boolean = ast match {
    case node: Node =>
      find(node, REPEATED).headOption match {
        case Some(_: Terminal) => true
        case _                 => false
      }
    case _ => false
  }

  def collectOptions(ast: Node, path: Seq[String], setterFn: DomainExtension => Unit)(implicit
      ctx: GrpcWebApiContext
  ): Unit = {
    collect(ast, path).map { case optNode: Node =>
      GrpcOptionParser(optNode).parse(setterFn)
    }
  }
}

object GrpcASTParserHelper extends GrpcASTParserHelper {
  val MAX_VALUE = 536870911
}
