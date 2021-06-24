package amf.plugins.document.apicontract.parser.spec.grpc

import amf.apicontract.internal.validation.definitions.ParserSideValidations
import amf.core.client.scala.model.domain.extensions.DomainExtension
import amf.core.client.scala.model.domain.{NamedDomainElement, Shape}
import amf.core.client.scala.parse.document.ParserContext
import amf.core.internal.annotations.{DeclaredElement, LexicalInformation}
import amf.core.internal.parser.domain.{Annotations, SearchScope}
import amf.plugins.document.apicontract.contexts.parser.grpc.GrpcWebApiContext
import amf.plugins.document.apicontract.parser.spec.domain.GrpcOptionParser
import amf.plugins.document.apicontract.parser.spec.grpc.TokenTypes._
import amf.shapes.client.scala.model.domain._
import amf.shapes.internal.domain.parser.XsdTypeDefMapping
import amf.shapes.internal.spec.common.TypeDef
import amf.shapes.internal.spec.common.TypeDef._
import org.mulesoft.antlrast.ast.{ASTElement, Node, Terminal}

trait AntlrASTParserHelper {
  def find(node: Node, name: String): Seq[ASTElement] = node.children.filter(_.name == name)

  def withName(ast: Node, nametoken: String, element: NamedDomainElement, adopt: NamedDomainElement => Unit = {_ => })(implicit ctx: GrpcWebApiContext): Unit = {
    path(ast, Seq(nametoken, IDENTIFIER)).foreach { node =>
      withOptTerminal(node) {
        case Some(shapeName) =>
          element.withName(shapeName.value)
          adopt(element)
        case None              =>
          path(node, Seq(KEYWORDS)) match {
            case Some(keywordNode) =>
              withOptTerminal(keywordNode) {
                case Some(kw) =>
                  element.withName(kw.value)
                  adopt(element)
                case _        =>
                  adopt(element)
                  astError(element.id, s"missing Protobuf3 $nametoken", element.annotations)
              }
            case None              =>
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
        case None              =>
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
          case _       =>
            None
        }
      case _                   =>
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
          case _       =>
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
      case _                  =>
        None
    }
  }

  protected def parseObjectRange(n: ASTElement, literalReference: String)(implicit ctx: GrpcWebApiContext): AnyShape = {
    val topLevelAlias = ctx.topLevelPackageRef(literalReference).map(alias => Seq(alias)).getOrElse(Nil)
    val qualifiedReference = ctx.fullMessagePath(literalReference)
    val externalReference = s".${literalReference}" // absolute reference based on the assumption the reference is for an external package imported in the file
    ctx.declarations
      .findType(qualifiedReference, SearchScope.All) // local reference inside a nested message, transformed into a top-level for possibly nested type
      .orElse {  // top-level reference for a reference, using just the name + plus package
        topLevelAlias.headOption.flatMap { alias =>
          ctx.declarations.findType(alias, SearchScope.All)
        }
      }
      .orElse(ctx.globalSpace.get(externalReference)) // fully qualified reference for an external package that might be in the global space
      .orElse{ // fully qualified reference for this package that might have been defined in a different file, and thus might be registered in the global space
        topLevelAlias.headOption.flatMap { alias =>
          ctx.globalSpace.get(alias)
        }
      }
    match {
      case Some(s: NodeShape) =>
        s.link(literalReference, toAnnotations(n)).asInstanceOf[NodeShape].withName(literalReference, toAnnotations(n))
      case Some(s: ScalarShape) =>
        s.link(literalReference, toAnnotations(n)).asInstanceOf[ScalarShape].withName(literalReference, toAnnotations(n))
      case _                 =>
        val shape = UnresolvedShape(literalReference, toAnnotations(n))
        shape.withContext(ctx)
        shape.unresolvedAntlrAst(literalReference, Seq(qualifiedReference)++ topLevelAlias, ctx.rootContextDocument, n)
        shape
    }
  }


  private def parseScalarRange(n: ASTElement, scalarType: TypeDef, format: Option[String]): ScalarShape = {
    val scalar = ScalarShape(toAnnotations(n)).withDataType(XsdTypeDefMapping.xsd(scalarType))
    format match {
      case Some(format) => scalar.withFormat(format)
      case _            =>
    }
    scalar
  }

  private def parseIsRepeated(ast: ASTElement)(implicit grpcWebApiContext: GrpcWebApiContext): Boolean = {
    ast match {
      case node: Node =>
        find(node, REPEATED).headOption match {
          case Some(n: Terminal) => true
          case _                 => false
        }
      case _         => false
    }

  }



  def collect(node: ASTElement, names: Seq[String]): Seq[ASTElement] = {
    if (names.isEmpty) {
      Seq(node)
    } else {
      val nextName = names.head
      node match {
        case n: Node =>
          find(n, nextName).flatMap { nested =>
            collect(nested, names.tail)
          }
        case _       => Nil
      }
    }
  }

  def path(node: ASTElement, names: Seq[String]): Option[ASTElement] = {
    if (names.isEmpty) {
      Some(node)
    } else {
      val nextName = names.head
      node match {
        case n: Node =>
          find(n, nextName) match {
            case found: Seq[ASTElement] if found.length == 1 =>
              path(found.head, names.tail)
            case _                                           =>
              None
          }
        case _       =>
          None
      }
    }
  }
  def withNode[T](element: ASTElement)(f: Node => T)(implicit ctx: ParserContext): T = element match {
    case node: Node => f(node)
    case _          => throw new Exception(s"Unexpected AST terminal token $element")
  }

  def withOptTerminal[T](element: ASTElement)(f: Option[Terminal] => T)(implicit ctx: ParserContext): T = element match {
    case node: Node if node.children.length == 1 && node.children.head.isInstanceOf[Terminal] =>
      f(Some(node.children.head.asInstanceOf[Terminal]))
    case _                  =>
      f(None)
  }

  def toAnnotations(elem: ASTElement): Annotations = {
    val lexInfo = LexicalInformation(elem.start.line, elem.start.column, elem.end.line, elem.end.column)
    Annotations() ++= Set(lexInfo)
  }

  def astError(id: String, message: String, annotations: Annotations)(implicit ctx: ParserContext): Unit = {
    ctx.eh.violation(ParserSideValidations.InvalidAst, id, message, annotations)
  }

  def collectOptions(ast: Node, adopt: DomainExtension => Unit)(implicit ctx: GrpcWebApiContext): Unit = {
    collect(ast, Seq(OPTION_STATEMENT)).map { case optNode: Node =>
      GrpcOptionParser(optNode).parse(adopt)
    }
  }
}
