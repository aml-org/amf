package amf.grpc.internal.spec.parser.domain

import amf.core.client.scala.model.DataType
import amf.core.client.scala.model.domain.{DataNode, ObjectNode, ScalarNode}
import amf.core.client.scala.model.domain.extensions.{CustomDomainProperty, DomainExtension}
import amf.core.internal.metamodel.domain.extensions.DomainExtensionModel
import amf.core.internal.utils.AmfStrings
import amf.grpc.internal.spec.parser.context.GrpcWebApiContext
import amf.grpc.internal.spec.parser.syntax.GrpcASTParserHelper
import amf.grpc.internal.spec.parser.syntax.TokenTypes._
import org.mulesoft.antlrast.ast.{ASTNode, Node}

import scala.collection.mutable

case class GrpcOptionParser(ast: Node)(implicit ctx: GrpcWebApiContext) extends GrpcASTParserHelper {
  val extension: DomainExtension = DomainExtension(toAnnotations(ast))

  def parse(setterFn: DomainExtension => Unit = _ => ()): DomainExtension = {
    parseName()
    setterFn(extension)
    parseExtension()
    extension set CustomDomainProperty(toAnnotations(ast)) as DomainExtensionModel.DefinedBy
    extension
  }

  def parseName(): Unit = {
    path(ast, Seq(OPTION_NAME)) foreach { case node: Node =>
      val name = node.children.filter(n => n.isInstanceOf[Node]).head.asInstanceOf[Node].source
      extension.withName(name, toAnnotations(node))
    }
  }

  private def parseExtension() = {
    find(ast, CONSTANT).headOption match {
      case Some(constant: Node) =>
        val data = parseConstant(constant)
        extension set data as DomainExtensionModel.Extension
      case _ =>
        astError("Missing mandatory protobuf3 option constant value", toAnnotations(ast))
    }
  }

  private def parseConstant(constAst: Node): DataNode = {
    val ann        = toAnnotations(constAst)
    val identValue = constAst.source.replaceAll("\"", "")

    def createScalar(dataType: String): ScalarNode =
      ScalarNode(ann).withValue(identValue, ann).withDataType(dataType, ann)

    constAst.children.headOption.map(_.name) match {
      case Some(FULL_IDENTIFIER) =>
        val dataType = identValue match {
          case "true" | "false" => DataType.Boolean
          case _                => DataType.String
        }
        createScalar(dataType)
      case Some(BLOCK_LITERAL)  => parseBlockLiteral(constAst.children.head.asInstanceOf[Node])
      case Some(BOOL_LITERAL)   => createScalar(DataType.Boolean)
      case Some(STRING_LITERAL) => createScalar(DataType.String)
      case _ if constAst.children.exists(_.name == INT_LITERAL)   => createScalar(DataType.Integer)
      case _ if constAst.children.exists(_.name == FLOAT_LITERAL) => createScalar(DataType.Float)
      case _ =>
        astError(s"Unknown protobuf constant ${constAst.source}", ann)
        createScalar(DataType.String)
    }
  }

  private def parseBlockLiteral(constAst: Node): ObjectNode = {
    val ann = toAnnotations(constAst)
    val obj = ObjectNode(ann)
    blockPairs(constAst.children).foreach { case (key, value) =>
      obj.addProperty(key.urlEncoded, value, ann)
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
