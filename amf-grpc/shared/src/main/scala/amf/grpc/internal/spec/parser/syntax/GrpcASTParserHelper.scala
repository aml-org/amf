package amf.grpc.internal.spec.parser.syntax

import amf.antlr.client.scala.parse.syntax.AntlrASTParserHelper
import amf.core.client.scala.model.domain.extensions.DomainExtension
import amf.core.client.scala.model.domain.{NamedDomainElement, Shape}
import amf.core.internal.annotations.DeclaredElement
import amf.core.internal.parser.domain.SearchScope
import amf.grpc.internal.spec.parser.context.GrpcWebApiContext
import amf.grpc.internal.spec.parser.domain.GrpcOptionParser
import amf.grpc.internal.spec.parser.syntax.TokenTypes._
import amf.shapes.client.scala.model.domain._
import amf.shapes.internal.domain.parser.XsdTypeDefMapping
import amf.shapes.internal.spec.common.TypeDef
import amf.shapes.internal.spec.common.TypeDef._
import org.mulesoft.antlrast.ast.{ASTElement, Node, Terminal}
import org.mulesoft.lexer.SourceLocation

trait GrpcASTParserHelper extends AntlrASTParserHelper {

  def withName(ast: Node, nametoken: String, element: NamedDomainElement, adopt: NamedDomainElement => Unit = { _ => })(implicit ctx: GrpcWebApiContext): Unit = {
    path(ast, Seq(nametoken, IDENTIFIER)).foreach { node =>
      withOptTerminal(node) {
        case Some(shapeName) =>
          element.withName(shapeName.value)
          adopt(element)
        case None =>
          path(node, Seq(KEYWORDS)) match {
            case Some(keywordNode) =>
              withOptTerminal(keywordNode) {
                case Some(kw) =>
                  element.withName(kw.value)
                  adopt(element)
                case _ =>
                  adopt(element)
                  astError(element.id, s"missing Protobuf3 $nametoken", element.annotations)
              }
            case None =>
              adopt(element)
              astError(element.id, s"missing Protobuf3 $nametoken", element.annotations)
          }
      }
    }
  }

  def withDeclaredShape(ast: Node, nametoken: String, element: AnyShape, adopt: NamedDomainElement => Unit = { _ => })(implicit ctx: GrpcWebApiContext): Unit = {
    path(ast, Seq(nametoken, IDENTIFIER)).foreach { node =>
      withOptTerminal(node) {
        case Some(shapeName) =>
          element.withName(ctx.fullMessagePath(shapeName.value))
          element.withDisplayName(shapeName.value)
          adopt(element)
          ctx.declarations += element
          element.add(DeclaredElement())
        case None =>
          adopt(element)
          astError(element.id, s"missing Protobuf3 $nametoken", element.annotations)
      }
    }

  }

  def parseFieldNumber(ast: ASTElement)(implicit ctx: GrpcWebApiContext): Option[Int] = {
    path(ast, Seq(FIELD_NUMBER, INT_LITERAL)) match {
      case Some(n: ASTElement) =>
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

  def parseFieldRange(ast: ASTElement, field: String = FIELD_TYPE)(implicit ctx: GrpcWebApiContext): Option[Shape] = {
    path(ast, Seq(field)) match {
      case Some(n: ASTElement) =>
        // scalar or object range
        val shape: Option[Shape] = withOptTerminal(n) {
          case Some(t) =>
            t.value match {
              case "double" => Some(parseScalarRange(n, DoubleType, None))
              case "float" => Some(parseScalarRange(n, FloatType, None))
              case "int32" => Some(parseScalarRange(n, IntType, Some("int32")))
              case "int64" => Some(parseScalarRange(n, LongType, Some("int64")))
              case "uint32" => Some(parseScalarRange(n, IntType, Some("uint32")))
              case "uint64" => Some(parseScalarRange(n, LongType, Some("uint64")))
              case "sint32" => Some(parseScalarRange(n, IntType, Some("sint32")))
              case "sint64" => Some(parseScalarRange(n, LongType, Some("sint64")))
              case "fixed32" => Some(parseScalarRange(n, IntType, Some("fixed32")))
              case "fixed64" => Some(parseScalarRange(n, LongType, Some("fixed64")))
              case "sfixed32" => Some(parseScalarRange(n, IntType, Some("sfixed32")))
              case "sfixed64" => Some(parseScalarRange(n, LongType, Some("sfixed64")))
              case "bool" => Some(parseScalarRange(n, BoolType, None))
              case "string" => Some(parseScalarRange(n, StrType, None))
              case "bytes" => Some(parseScalarRange(n, ByteType, None))
              case _ => None
            }
          case _ =>
            path(n, Seq(MESSAGE_TYPE)).flatMap { case messageRef: Node =>
              Some(parseObjectRange(n, messageRef.source))
            }
        }
        // check if array
        shape map { shape =>
          if (parseIsRepeated(ast)) {
            ArrayShape(toAnnotations(ast)).withItems(shape)
          } else {
            shape
          }
        }
      case _ =>
        None
    }
  }

  protected def parseObjectRange(n: ASTElement, literalReference: String)(implicit ctx: GrpcWebApiContext): AnyShape = {
    val topLevelAlias = ctx.topLevelPackageRef(literalReference).map(alias => Seq(alias)).getOrElse(Nil)
    val qualifiedReference = ctx.fullMessagePath(literalReference)
    val externalReference = s".${literalReference}" // absolute reference based on the assumption the reference is for an external package imported in the file
    ctx.declarations
      .findType(qualifiedReference, SearchScope.All) // local reference inside a nested message, transformed into a top-level for possibly nested type
      .orElse { // top-level reference for a reference, using just the name + plus package
        topLevelAlias.headOption.flatMap { alias =>
          ctx.declarations.findType(alias, SearchScope.All)
        }
      }
      .orElse(ctx.globalSpace.get(externalReference)) // fully qualified reference for an external package that might be in the global space
      .orElse { // fully qualified reference for this package that might have been defined in a different file, and thus might be registered in the global space
        topLevelAlias.headOption.flatMap { alias =>
          ctx.globalSpace.get(alias)
        }
      }
    match {
      case Some(s: NodeShape) =>
        s.link(literalReference, toAnnotations(n)).asInstanceOf[NodeShape].withName(literalReference, toAnnotations(n))
      case Some(s: ScalarShape) =>
        s.link(literalReference, toAnnotations(n)).asInstanceOf[ScalarShape].withName(literalReference, toAnnotations(n))
      case _ =>
        val shape = UnresolvedShape(literalReference, toAnnotations(n))
        shape.withContext(ctx)
        shape.unresolved(literalReference, Seq(qualifiedReference) ++ topLevelAlias, Some(new SourceLocation(n.file, 0, 0, n.start.line, n.start.column, n.end.line, n.end.column)))
        shape
    }
  }


  private def parseScalarRange(n: ASTElement, scalarType: TypeDef, format: Option[String]): ScalarShape = {
    val scalar = ScalarShape(toAnnotations(n)).withDataType(XsdTypeDefMapping.xsd(scalarType))
    format match {
      case Some(format) => scalar.withFormat(format)
      case _ =>
    }
    scalar
  }

  private def parseIsRepeated(ast: ASTElement)(implicit grpcWebApiContext: GrpcWebApiContext): Boolean = {
    ast match {
      case node: Node =>
        find(node, REPEATED).headOption match {
          case Some(n: Terminal) => true
          case _ => false
        }
      case _ => false
    }

  }

  def collectOptions(ast: Node, path: Seq[String], adopt: DomainExtension => Unit)(implicit ctx: GrpcWebApiContext): Unit = {
    collect(ast, path).map { case optNode: Node =>
      GrpcOptionParser(optNode).parse(adopt)
    }
  }
}
