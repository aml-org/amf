package amf.grpc.internal.spec.parser.domain

import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.grpc.internal.spec.parser.context.GrpcWebApiContext
import amf.grpc.internal.spec.parser.syntax.GrpcASTParserHelper
import amf.grpc.internal.spec.parser.syntax.TokenTypes._
import amf.shapes.client.scala.model.domain.{NodeShape, UnionShape}
import org.mulesoft.antlrast.ast.Node

class GrpcMessageParser(ast: Node)(implicit val ctx: GrpcWebApiContext) extends GrpcASTParserHelper {
  val nodeShape: NodeShape = NodeShape(toAnnotations(ast))

  def parse(setterFn: NodeShape => Unit = _ => ()): NodeShape = {
    parseName()
    setterFn(nodeShape)
    parseMessageBody()
    nodeShape
  }

  def parseName(): Unit =
    withDeclaredShape(ast, MESSAGE_NAME, nodeShape)

  private def parseMessageBody(): Unit = {
    collect(ast, Seq(MESSAGE_BODY, MESSAGE_ELEMENT)).foreach { case messageElement: Node =>
      val messageElementAst = messageElement.children.head.asInstanceOf[Node]
      val context           = ctx.nestedMessage(nodeShape.displayName.value())
      messageElementAst.name match {
        case FIELD =>
          GrpcFieldParser(messageElementAst)(context).parse(property => {
            nodeShape.withProperties(nodeShape.properties ++ Seq(property))
          })
        case ENUM_DEF =>
          GrpcEnumParser(messageElementAst)(context).parse()
        case ONE_OF =>
          GrpcOneOfParser(messageElementAst)(context).parse { union: UnionShape =>
            nodeShape.withAnd(nodeShape.and ++ Seq(union))
          }
        case MAP_FIELD =>
          GrpcMapParser(messageElementAst)(context).parse { mapProperty: PropertyShape =>
            nodeShape.withProperties(nodeShape.properties ++ Seq(mapProperty))
          }
        case MESSAGE_DEF =>
          GrpcMessageParser(messageElementAst)(context).parse()
        case OPTION_STATEMENT =>
          GrpcOptionParser(messageElementAst).parse { extension => nodeShape.withCustomDomainProperty(extension) }
        case RESERVED =>
          GrpcReservedValuesParser(messageElementAst).parse { reservedValues =>
            nodeShape.withReservedValues(reservedValues)
          }
        case _ =>
          astError(
            s"unexpected Proto3 message element ${messageElement.children.head.name}",
            toAnnotations(messageElement.children.head)
          )
      }
    }
  }

}

object GrpcMessageParser {
  def apply(ast: Node)(implicit ctx: GrpcWebApiContext) = new GrpcMessageParser(ast)
}
