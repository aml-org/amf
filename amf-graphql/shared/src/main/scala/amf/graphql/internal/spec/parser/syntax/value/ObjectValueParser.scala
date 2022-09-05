package amf.graphql.internal.spec.parser.syntax.value

import amf.core.client.scala.model.domain.{ArrayNode, DataNode, ObjectNode}
import amf.graphql.internal.spec.context.GraphQLBaseWebApiContext
import amf.graphql.internal.spec.parser.syntax.{GraphQLASTParserHelper, ValueParser}
import amf.graphql.internal.spec.parser.syntax.TokenTypes.{LIST_VALUE, OBJECT_FIELD, OBJECT_VALUE, VALUE}
import org.mulesoft.antlrast.ast.Node

object ObjectValueParser extends AbstractValueParser[ObjectNode] with GraphQLASTParserHelper {
  override def parse(node: Node, path: Seq[String])(implicit ctx: GraphQLBaseWebApiContext): Option[ObjectNode] = {
    pathToNonTerminal(node, path ++ Seq(OBJECT_VALUE)).map { objAst =>
      parseObjectNode(objAst)
    }
  }

  private def parseObjectNode(objAst: Node)(implicit ctx: GraphQLBaseWebApiContext) = {
    val obj = ObjectNode(toAnnotations(objAst))
    collectNodes(objAst, Seq(OBJECT_FIELD)).map { fieldAst =>
      parseObjectField(obj, fieldAst)
    }
    obj
  }

  private def parseObjectField(obj: ObjectNode, fieldAst: Node)(implicit ctx: GraphQLBaseWebApiContext) = {
    val (name, _) = findName(fieldAst, "err", "Error")
    parseFieldValue(fieldAst) match {
      case Some(value) => obj.addProperty(name, value, toAnnotations(fieldAst))
      case _           => //
    }
  }

  private def parseFieldValue(n: Node)(implicit ctx: GraphQLBaseWebApiContext): Option[DataNode] = {
    pathToNonTerminal(n, Seq(VALUE)).flatMap(ValueParser.parseValue(_))
  }
}
