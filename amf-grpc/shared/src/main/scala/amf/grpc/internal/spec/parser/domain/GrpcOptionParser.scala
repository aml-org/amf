package amf.grpc.internal.spec.parser.domain

import amf.core.client.scala.model.DataType
import amf.core.client.scala.model.domain.{DataNode, ObjectNode, ScalarNode}
import amf.core.client.scala.model.domain.extensions.{CustomDomainProperty, DomainExtension}
import amf.core.client.scala.vocabulary.Namespace
import amf.core.internal.utils.AmfStrings
import amf.grpc.internal.spec.parser.context.GrpcWebApiContext
import amf.grpc.internal.spec.parser.syntax.GrpcASTParserHelper
import amf.grpc.internal.spec.parser.syntax.TokenTypes._
import org.mulesoft.antlrast.ast.{ASTNode, Node}

import scala.collection.mutable

case class GrpcOptionParser(ast: Node)(implicit ctx: GrpcWebApiContext) extends GrpcASTParserHelper {
  val extension: DomainExtension = DomainExtension(toAnnotations(ast))

  def parse(setterFn: DomainExtension => Unit): DomainExtension = {
    parseName()
    setterFn(extension)
    parseExtension()
    extension.withDefinedBy(CustomDomainProperty(toAnnotations(ast)))
    extension
  }

  def parseName(): Unit = {
    path(ast, Seq(OPTION_NAME)) foreach { case node: Node =>
      extension.withName(node.children.filter(n => n.isInstanceOf[Node]).head.asInstanceOf[Node].source)
    }
  }

  private def parseExtension() = {
    find(ast, CONSTANT).headOption match {
      case Some(constant: Node) =>
        val data = parseConstant(constant)
        extension.withExtension(data)
      case _ =>
        astError("Missing mandatory protobuf3 option constant value", toAnnotations(ast))
    }
  }

  private def parseConstant(constAst: Node): DataNode = {
    if (constAst.children.head.name == FULL_IDENTIFIER) {
      val identValue = normalize(constAst.source)
      val s          = ScalarNode(toAnnotations(constAst)).withValue(identValue)
      identValue match {
        case "true"  => s.withDataType(DataType.Boolean)
        case "false" => s.withDataType(DataType.Boolean)
        case _       => s.withDataType(DataType.String)
      }
      s
    } else if (constAst.children.head.name == BLOCK_LITERAL) {
      parseBlockLiteral(constAst.children.head.asInstanceOf[Node])
    } else if (constAst.children.exists(_.name == INT_LITERAL)) {
      val s = ScalarNode(toAnnotations(constAst)).withValue(normalize(constAst.source)).withDataType(DataType.Integer)
      s
    } else if (constAst.children.exists(_.name == FLOAT_LITERAL)) {
      val s = ScalarNode(toAnnotations(constAst)).withValue(normalize(constAst.source)).withDataType(DataType.Float)
      s
    } else if (constAst.children.head.name == BOOL_LITERAL) {
      val s = ScalarNode(toAnnotations(constAst)).withValue(normalize(constAst.source)).withDataType(DataType.Boolean)
      s
    } else if (constAst.children.head.name == STRING_LITERAL) {
      val s = ScalarNode(toAnnotations(constAst)).withValue(normalize(constAst.source)).withDataType(DataType.String)
      s
    } else {
      val s = ScalarNode(toAnnotations(constAst)).withValue(normalize(constAst.source)).withDataType(DataType.String)
      astError(s"Unknown protobuf constant ${constAst.source}", toAnnotations(constAst))
      s
    }
  }

  private def normalize(s: String) = s.replaceAll("\"", "")

  private def parseBlockLiteral(constAst: Node): ObjectNode = {
    val obj = ObjectNode(toAnnotations(constAst))
    blockPairs(constAst.children).foreach { case (key, value) =>
      obj.addProperty(key.urlEncoded, value)
    }
    obj
  }

  def blockPairs(nodes: Seq[ASTNode]): Seq[(String, DataNode)] = {
    val acc: mutable.Buffer[(String, DataNode)] = mutable.Buffer()
    var nextKey: Option[String]                 = None
    nodes.foreach {
      case n: Node =>
        n.name match {
          case IDENTIFIER =>
            nextKey = Some(n.source)
          case CONSTANT =>
            val nextValue = parseConstant(n)
            acc.append((nextKey.get, nextValue))
        }
      case _ => // ignore
    }
    acc
  }
}
