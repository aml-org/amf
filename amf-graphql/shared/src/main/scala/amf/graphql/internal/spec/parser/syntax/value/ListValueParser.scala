package amf.graphql.internal.spec.parser.syntax.value
import amf.graphql.internal.spec.document._

import amf.core.client.scala.model.domain.{AmfArray, ArrayNode}
import amf.core.internal.metamodel.domain.ArrayNodeModel
import amf.core.internal.parser.domain.Annotations.inferred
import amf.graphql.internal.spec.context.GraphQLBaseWebApiContext
import amf.graphql.internal.spec.parser.syntax.{GraphQLASTParserHelper, ValueParser}
import amf.graphql.internal.spec.parser.syntax.TokenTypes.{LIST_VALUE, VALUE}
import org.mulesoft.antlrast.ast.Node

object ListValueParser extends AbstractValueParser[ArrayNode] with GraphQLASTParserHelper {
  override def parse(node: Node, path: Seq[String])(implicit ctx: GraphQLBaseWebApiContext): Option[ArrayNode] = {
    pathToNonTerminal(node, path ++ Seq(LIST_VALUE)).map { listAst =>
      parseListValue(listAst)
    }
  }

  private def parseListValue(listAst: Node)(implicit ctx: GraphQLBaseWebApiContext): ArrayNode = {
    val members = collectNodes(listAst, Seq(VALUE)).flatMap(member => ValueParser.parseValue(member))
    val ann     = toAnnotations(listAst)
    ArrayNode(ann) set AmfArray(members, ann) as ArrayNodeModel.Member
  }
}
