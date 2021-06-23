package amf.plugins.document.apicontract.parser.spec.domain

import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.plugins.document.apicontract.contexts.parser.grpc.GrpcWebApiContext
import amf.plugins.document.apicontract.parser.spec.grpc.AntlrASTParserHelper
import amf.plugins.document.apicontract.parser.spec.grpc.TokenTypes._
import amf.shapes.client.scala.model.domain.{NodeShape, UnionShape}
import org.mulesoft.antlrast.ast.Node

class GrpcMessageParser(ast: Node)(implicit val ctx: GrpcWebApiContext) extends AntlrASTParserHelper {
  val nodeShape: NodeShape = NodeShape(toAnnotations(ast))

  def parse(adopt: NodeShape => Unit): NodeShape = {
    parseName(adopt)
    parseMessageBody()
    nodeShape
  }

  def parseName(adopt: NodeShape => Unit): Unit = withDeclaredShape(ast, MESSAGE_NAME, nodeShape, { _ => adopt(nodeShape)})

  def parseMessageBody(): Unit = {
    collect(ast, Seq(MESSAGE_BODY, MESSAGE_ELEMENT)).foreach { case messageElement: Node =>
      val messageElementAst = messageElement.children.head.asInstanceOf[Node]
      messageElementAst.name match {
        case FIELD =>
          GrpcFieldParser(messageElementAst)(ctx.nestedMessage(nodeShape.displayName.value())).parse((property) => {
            property.adopted(nodeShape.id)
            nodeShape.withProperties(nodeShape.properties ++ Seq(property))
          })
        case ENUM_DEF =>
          GrpcEnumParser(messageElementAst)(ctx.nestedMessage(nodeShape.displayName.value())).parse(enum => {
            enum.adopted(nodeShape.id)
          })
        case ONE_OF =>
          GrpcOneOfParser(messageElementAst)(ctx.nestedMessage(nodeShape.displayName.value())).parse({ union: UnionShape =>
            union.adopted(nodeShape.id)
            nodeShape.withAnd(nodeShape.and ++ Seq(union))
          })
        case MAP_FIELD =>
          GrpcMapParser(messageElementAst)(ctx.nestedMessage(nodeShape.displayName.value())).parse({ mapProperty: PropertyShape =>
            mapProperty.adopted(nodeShape.id)
            nodeShape.withProperties(nodeShape.properties ++ Seq(mapProperty))
          })
        case MESSAGE_DEF =>
          GrpcMessageParser(messageElementAst)(ctx.nestedMessage(nodeShape.displayName.value())).parse({nestedNodeShape: NodeShape =>
            nestedNodeShape.adopted(nodeShape.id)
          })
        case _        =>
          astError(nodeShape.id, s"unexpected Proto3 message element ${messageElement.children.head.name}", toAnnotations(messageElement.children.head))
      }
    }
  }

}

object GrpcMessageParser {
  def apply(ast: Node)(implicit ctx: GrpcWebApiContext) = new GrpcMessageParser(ast)
}